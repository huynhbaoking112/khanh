package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.domain.model.Reader;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class ReaderManagementServiceImpl implements ReaderManagementService {
    private final ReaderRepository readerRepository;
    private final LoanDetailRepository loanDetailRepository;

    public ReaderManagementServiceImpl(
            ReaderRepository readerRepository,
            LoanDetailRepository loanDetailRepository) {
        this.readerRepository = readerRepository;
        this.loanDetailRepository = loanDetailRepository;
    }

    @Override
    public List<Reader> listReaders() {
        try {
            return readerRepository.findAll();
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai danh sach doc gia", exception);
        }
    }

    @Override
    public Optional<Reader> getReader(String readerId) {
        try {
            return readerRepository.findById(readerId);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai thong tin doc gia", exception);
        }
    }

    @Override
    public Reader createReader(Reader reader) {
        validate(reader);
        try {
            if (readerRepository.findById(reader.readerId()).isPresent()) {
                throw new BusinessRuleViolationException("Ma doc gia da ton tai");
            }
            return readerRepository.save(reader);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tao doc gia", exception);
        }
    }

    @Override
    public Reader updateReader(Reader reader) {
        validate(reader);
        try {
            if (readerRepository.findById(reader.readerId()).isEmpty()) {
                throw new BusinessRuleViolationException("Khong tim thay doc gia can cap nhat");
            }
            return readerRepository.update(reader);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the cap nhat doc gia", exception);
        }
    }

    @Override
    public int getCurrentBorrowCount(String readerId) {
        try {
            return loanDetailRepository.countActiveBorrowedBooksForReader(readerId);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tinh so sach dang muon", exception);
        }
    }

    private void validate(Reader reader) {
        if (reader == null || isBlank(reader.readerId()) || isBlank(reader.fullName())) {
            throw new BusinessRuleViolationException("Thong tin doc gia khong hop le");
        }
        // Phone is required according to spec V6
        if (isBlank(reader.phone())) {
            throw new BusinessRuleViolationException("So dien thoai bat buoc");
        }
        if (reader.maxBorrow() < 0) {
            throw new BusinessRuleViolationException("Gioi han muon khong hop le");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
