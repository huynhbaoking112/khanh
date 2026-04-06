package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.domain.model.Book;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public final class BookCatalogServiceImpl implements BookCatalogService {
    private final BookRepository bookRepository;

    public BookCatalogServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> listBooks() {
        try {
            return bookRepository.findAll();
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai danh muc sach", exception);
        }
    }

    @Override
    public List<Book> searchBooks(String keyword, boolean onlyAvailable) {
        try {
            return bookRepository.search(keyword, onlyAvailable);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tim kiem sach", exception);
        }
    }

    @Override
    public Optional<Book> getBook(String bookId) {
        try {
            return bookRepository.findById(bookId);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tai thong tin sach", exception);
        }
    }

    @Override
    public Book createBook(Book book) {
        validate(book);
        try {
            if (bookRepository.findById(book.bookId()).isPresent()) {
                throw new BusinessRuleViolationException("Ma sach da ton tai");
            }
            return bookRepository.save(book);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the tao sach moi", exception);
        }
    }

    @Override
    public Book updateBook(Book book) {
        validate(book);
        try {
            if (bookRepository.findById(book.bookId()).isEmpty()) {
                throw new BusinessRuleViolationException("Khong tim thay sach can cap nhat");
            }
            return bookRepository.update(book);
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the cap nhat sach", exception);
        }
    }

    private void validate(Book book) {
        if (book == null || isBlank(book.bookId()) || isBlank(book.title()) || isBlank(book.author())) {
            throw new BusinessRuleViolationException("Thong tin sach khong hop le");
        }
        if (book.quantityTotal() < 0 || book.quantityAvailable() < 0 || book.quantityAvailable() > book.quantityTotal()) {
            throw new BusinessRuleViolationException("So luong sach khong hop le");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
