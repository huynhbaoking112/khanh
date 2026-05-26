package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.application.repository.FineRepository;
import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.repository.LoanRepository;
import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.dto.LoanReceipt;
import com.thayhoang.quanly.application.service.dto.ReturnReceipt;
import com.thayhoang.quanly.application.support.IdentifierGenerator;
import com.thayhoang.quanly.domain.enums.FinePaymentStatus;
import com.thayhoang.quanly.domain.enums.LoanStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.model.Fine;
import com.thayhoang.quanly.domain.model.Loan;
import com.thayhoang.quanly.domain.model.LoanDetail;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.rules.CirculationRules;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class CirculationServiceImpl implements CirculationService {
    private final ReaderRepository readerRepository;
    private final LibrarianRepository librarianRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final FineRepository fineRepository;

    public CirculationServiceImpl(
            ReaderRepository readerRepository,
            LibrarianRepository librarianRepository,
            BookRepository bookRepository,
            LoanRepository loanRepository,
            LoanDetailRepository loanDetailRepository,
            FineRepository fineRepository) {
        this.readerRepository = readerRepository;
        this.librarianRepository = librarianRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.loanDetailRepository = loanDetailRepository;
        this.fineRepository = fineRepository;
    }

    @Override
    public LoanReceipt createLoan(String readerId, String librarianId, List<String> bookIds) {
        List<String> normalizedBookIds = normalizeBookIds(bookIds);
        if (normalizedBookIds.isEmpty()) {
            throw new BusinessRuleViolationException("Danh sach sach muon khong duoc rong");
        }

        Set<String> distinctBookIds = new LinkedHashSet<>(normalizedBookIds);
        if (distinctBookIds.size() != normalizedBookIds.size()) {
            throw new BusinessRuleViolationException("Moi sach chi duoc chon mot lan trong phieu muon");
        }

        try (Connection connection = DatabaseSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Reader reader = readerRepository.findById(connection, readerId)
                        .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay doc gia"));
                librarianRepository.findById(connection, librarianId)
                        .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay thu thu"));

                int currentBorrowCount = loanDetailRepository.countActiveBorrowedBooksForReader(connection, readerId);
                CirculationRules.validateReaderForBorrowing(reader, currentBorrowCount, distinctBookIds.size());

                List<Book> booksToBorrow = new ArrayList<>();
                for (String bookId : distinctBookIds) {
                    Book book = bookRepository.findById(connection, bookId)
                            .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay sach " + bookId));
                    CirculationRules.validateBookAvailability(book, 1);
                    booksToBorrow.add(book);
                }

                LocalDate loanDate = LocalDate.now();
                Loan loan = new Loan(
                        IdentifierGenerator.next("LOAN"),
                        readerId,
                        librarianId,
                        loanDate,
                        CirculationRules.defaultDueDate(loanDate),
                        null,
                        LoanStatus.ACTIVE);
                loanRepository.save(connection, loan);

                List<LoanDetail> details = new ArrayList<>();
                for (Book book : booksToBorrow) {
                    LoanDetail detail = new LoanDetail(
                            IdentifierGenerator.next("LD"),
                            loan.loanId(),
                            book.bookId(),
                            1,
                            false,
                            null);
                    loanDetailRepository.save(connection, detail);
                    details.add(detail);

                    bookRepository.update(connection, new Book(
                            book.bookId(),
                            book.title(),
                            book.author(),
                            book.publisher(),
                            book.yearPublish(),
                            book.quantityTotal(),
                            book.quantityAvailable() - 1,
                            book.status()));
                }

                connection.commit();
                return new LoanReceipt(loan, List.copyOf(details));
            } catch (Exception exception) {
                rollback(connection);
                if (exception instanceof ApplicationException applicationException) {
                    throw applicationException;
                }
                throw new ApplicationException("Khong the tao phieu muon", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tao phieu muon", exception);
        }
    }

    @Override
    public ReturnReceipt returnBooks(String loanId, List<String> returnedBookIds, LocalDate returnDate) {
        List<String> normalizedBookIds = normalizeBookIds(returnedBookIds);
        if (normalizedBookIds.isEmpty()) {
            throw new BusinessRuleViolationException("Danh sach sach tra khong duoc rong");
        }

        Set<String> distinctBookIds = new LinkedHashSet<>(normalizedBookIds);
        if (distinctBookIds.size() != normalizedBookIds.size()) {
            throw new BusinessRuleViolationException("Danh sach sach tra bi trung lap");
        }

        LocalDate effectiveReturnDate = returnDate == null ? LocalDate.now() : returnDate;

        try (Connection connection = DatabaseSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanRepository.findById(connection, loanId)
                        .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay phieu muon"));
                CirculationRules.validateLoanCanAcceptReturns(loan);

                List<LoanDetail> allDetails = new ArrayList<>(loanDetailRepository.findByLoanId(connection, loanId));

                for (String bookId : distinctBookIds) {
                    LoanDetail detail = loanDetailRepository.findByLoanAndBookId(connection, loanId, bookId)
                            .orElseThrow(() -> new BusinessRuleViolationException("Sach " + bookId + " khong nam trong phieu muon"));
                    CirculationRules.validateLoanDetailCanBeReturned(detail);

                    LoanDetail updatedDetail = new LoanDetail(
                            detail.loanDetailId(),
                            detail.loanId(),
                            detail.bookId(),
                            detail.quantity(),
                            true,
                            detail.note());
                    loanDetailRepository.update(connection, updatedDetail);
                    replaceDetail(allDetails, updatedDetail);

                    Book book = bookRepository.findById(connection, detail.bookId())
                            .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay sach de cap nhat ton kho"));
                    bookRepository.update(connection, new Book(
                            book.bookId(),
                            book.title(),
                            book.author(),
                            book.publisher(),
                            book.yearPublish(),
                            book.quantityTotal(),
                            book.quantityAvailable() + detail.quantity(),
                            book.status()));
                }

                boolean allReturned = allDetails.stream().allMatch(LoanDetail::returned);
                Loan updatedLoan = new Loan(
                        loan.loanId(),
                        loan.readerId(),
                        loan.librarianId(),
                        loan.loanDate(),
                        loan.dueDate(),
                        allReturned ? effectiveReturnDate : loan.returnDate(),
                        CirculationRules.resolveLoanStatusAfterReturn(allReturned, loan.dueDate(), effectiveReturnDate));
                loanRepository.update(connection, updatedLoan);

                Optional<Fine> fine = Optional.empty();
                BigDecimal fineAmount = CirculationRules.calculateFine(loan.dueDate(), effectiveReturnDate);
                if (fineAmount.signum() > 0) {
                    Fine fineToSave = new Fine(
                            fineRepository.findByLoanId(connection, loan.loanId())
                                    .map(Fine::fineId)
                                    .orElse(IdentifierGenerator.next("FINE")),
                            loan.loanId(),
                            fineAmount,
                            "Tra sach tre han",
                            FinePaymentStatus.UNPAID);
                    fine = Optional.of(fineRepository.saveOrUpdate(connection, fineToSave));
                }

                connection.commit();
                return new ReturnReceipt(updatedLoan, List.copyOf(allDetails), fine);
            } catch (Exception exception) {
                rollback(connection);
                if (exception instanceof ApplicationException applicationException) {
                    throw applicationException;
                }
                throw new ApplicationException("Khong the xu ly tra sach", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the xu ly tra sach", exception);
        }
    }

    @Override
    public Loan renewLoan(String loanId, int extraDays, LocalDate referenceDate) {
        LocalDate effectiveReferenceDate = referenceDate == null ? LocalDate.now() : referenceDate;

        try (Connection connection = DatabaseSupport.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Loan loan = loanRepository.findById(connection, loanId)
                        .orElseThrow(() -> new BusinessRuleViolationException("Khong tim thay phieu muon"));
                List<LoanDetail> details = loanDetailRepository.findByLoanId(connection, loanId);
                CirculationRules.validateLoanRenewal(loan, details, extraDays, effectiveReferenceDate);

                Loan renewedLoan = new Loan(
                        loan.loanId(),
                        loan.readerId(),
                        loan.librarianId(),
                        loan.loanDate(),
                        loan.dueDate().plusDays(extraDays),
                        loan.returnDate(),
                        loan.status());
                loanRepository.update(connection, renewedLoan);
                connection.commit();
                return renewedLoan;
            } catch (Exception exception) {
                rollback(connection);
                if (exception instanceof ApplicationException applicationException) {
                    throw applicationException;
                }
                throw new ApplicationException("Khong the gia han phieu muon", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the gia han phieu muon", exception);
        }
    }

    private List<String> normalizeBookIds(List<String> bookIds) {
        if (bookIds == null) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (String bookId : bookIds) {
            if (bookId != null && !bookId.trim().isEmpty()) {
                normalized.add(bookId.trim());
            }
        }
        return List.copyOf(normalized);
    }

    private void replaceDetail(List<LoanDetail> details, LoanDetail updatedDetail) {
        for (int index = 0; index < details.size(); index++) {
            if (details.get(index).loanDetailId().equals(updatedDetail.loanDetailId())) {
                details.set(index, updatedDetail);
                return;
            }
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
        }
    }
}
