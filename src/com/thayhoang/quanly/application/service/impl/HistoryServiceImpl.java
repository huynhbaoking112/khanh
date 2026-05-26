package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.repository.FineRepository;
import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.repository.LoanRepository;
import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.model.Loan;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HistoryServiceImpl implements HistoryService {
    private final ReaderRepository readerRepository;
    private final LibrarianRepository librarianRepository;
    private final LoanRepository loanRepository;
    private final LoanDetailRepository loanDetailRepository;
    private final FineRepository fineRepository;

    public HistoryServiceImpl(
            ReaderRepository readerRepository,
            LibrarianRepository librarianRepository,
            LoanRepository loanRepository,
            LoanDetailRepository loanDetailRepository,
            FineRepository fineRepository) {
        this.readerRepository = readerRepository;
        this.librarianRepository = librarianRepository;
        this.loanRepository = loanRepository;
        this.loanDetailRepository = loanDetailRepository;
        this.fineRepository = fineRepository;
    }

    @Override
    public List<LoanRecord> getReaderHistory(String readerId) {
        try {
            Optional<com.thayhoang.quanly.domain.model.Reader> reader = readerRepository.findById(readerId);
            if (reader.isEmpty()) {
                return List.of();
            }

            List<LoanRecord> records = new ArrayList<>();
            for (Loan loan : loanRepository.findByReaderId(readerId)) {
                Optional<com.thayhoang.quanly.domain.model.Librarian> librarian =
                        librarianRepository.findById(loan.librarianId());
                if (librarian.isEmpty()) {
                    continue;
                }
                records.add(new LoanRecord(
                        loan,
                        reader.get(),
                        librarian.get(),
                        loanDetailRepository.findByLoanId(loan.loanId()),
                        fineRepository.findByLoanId(loan.loanId())));
            }
            return List.copyOf(records);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai lich su muon cua doc gia", exception);
        }
    }

    @Override
    public Optional<LoanRecord> getLoanRecord(String loanId) {
        try {
            Optional<Loan> loan = loanRepository.findById(loanId);
            if (loan.isEmpty()) {
                return Optional.empty();
            }

            Loan foundLoan = loan.get();
            Optional<com.thayhoang.quanly.domain.model.Reader> reader = readerRepository.findById(foundLoan.readerId());
            Optional<com.thayhoang.quanly.domain.model.Librarian> librarian =
                    librarianRepository.findById(foundLoan.librarianId());
            if (reader.isEmpty() || librarian.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new LoanRecord(
                    foundLoan,
                    reader.get(),
                    librarian.get(),
                    loanDetailRepository.findByLoanId(foundLoan.loanId()),
                    fineRepository.findByLoanId(foundLoan.loanId())));
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai chi tiet phieu muon", exception);
        }
    }
}
