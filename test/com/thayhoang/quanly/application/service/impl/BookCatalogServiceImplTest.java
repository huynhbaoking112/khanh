package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.domain.model.Book;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class BookCatalogServiceImplTest {

    private Book sampleBook() {
        return new Book("B1", "T", "A", "P", 2020, 2, 2, com.thayhoang.quanly.domain.enums.BookStatus.AVAILABLE);
    }

    @Test
    public void listBooks_handlesSQLException() {
        BookRepository repo = new BookRepository() {
            @Override public Optional<Book> findById(String bookId) throws SQLException { return Optional.empty(); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) throws SQLException { return Optional.empty(); }
            @Override public java.util.List<Book> findAll() throws SQLException { throw new SQLException("fail"); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) throws SQLException { return List.of(); }
            @Override public Book save(Book book) throws SQLException { return book; }
            @Override public Book save(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public Book update(Book book) throws SQLException { return book; }
            @Override public Book update(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public int count() throws SQLException { return 0; }
        };
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);
        ApplicationException ex = assertThrows(ApplicationException.class, svc::listBooks);
        assertTrue(ex.getMessage().contains("Khong the tai danh muc sach"));
    }

    @Test
    public void createBook_success_and_duplicate() throws SQLException {
        Book book = sampleBook();
        BookRepository repo = new BookRepository() {
            boolean saved = false;
            @Override public Optional<Book> findById(String bookId) throws SQLException { return Optional.empty(); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) throws SQLException { return Optional.empty(); }
            @Override public java.util.List<Book> findAll() throws SQLException { return List.of(); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) throws SQLException { return List.of(); }
            @Override public Book save(Book b) throws SQLException { saved = true; return b; }
            @Override public Book save(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public Book update(Book book) throws SQLException { return book; }
            @Override public Book update(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public int count() throws SQLException { return 0; }
        };
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);
        Book created = svc.createBook(book);
        assertEquals(book.bookId(), created.bookId());

        // duplicate check
        BookRepository repo2 = new BookRepository() {
            @Override public Optional<Book> findById(String bookId) throws SQLException { return Optional.of(book); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) throws SQLException { return Optional.of(book); }
            @Override public java.util.List<Book> findAll() throws SQLException { return List.of(); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) throws SQLException { return List.of(); }
            @Override public Book save(Book b) throws SQLException { return b; }
            @Override public Book save(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public Book update(Book book) throws SQLException { return book; }
            @Override public Book update(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public int count() throws SQLException { return 0; }
        };
        BookCatalogServiceImpl svc2 = new BookCatalogServiceImpl(repo2);
        assertThrows(BusinessRuleViolationException.class, () -> svc2.createBook(book));
    }

    @Test
    public void updateBook_notFound_and_success() throws SQLException {
        Book book = sampleBook();
        BookRepository repo = new BookRepository() {
            @Override public Optional<Book> findById(String bookId) throws SQLException { return Optional.empty(); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) throws SQLException { return Optional.empty(); }
            @Override public java.util.List<Book> findAll() throws SQLException { return List.of(); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) throws SQLException { return List.of(); }
            @Override public Book save(Book b) throws SQLException { return b; }
            @Override public Book save(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public Book update(Book b) throws SQLException { return b; }
            @Override public Book update(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public int count() throws SQLException { return 0; }
        };
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);
        assertThrows(BusinessRuleViolationException.class, () -> svc.updateBook(book));

        BookRepository repo2 = new BookRepository() {
            @Override public Optional<Book> findById(String bookId) throws SQLException { return Optional.of(book); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) throws SQLException { return Optional.of(book); }
            @Override public java.util.List<Book> findAll() throws SQLException { return List.of(); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) throws SQLException { return List.of(); }
            @Override public Book save(Book b) throws SQLException { return b; }
            @Override public Book save(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public Book update(Book b) throws SQLException { return b; }
            @Override public Book update(java.sql.Connection connection, Book book) throws SQLException { return book; }
            @Override public int count() throws SQLException { return 0; }
        };
        BookCatalogServiceImpl svc2 = new BookCatalogServiceImpl(repo2);
        Book updated = svc2.updateBook(book);
        assertEquals(book.bookId(), updated.bookId());
    }

    @Test
    public void validate_rejects_invalid_books() {
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(new BookRepository() {
            @Override public Optional<Book> findById(String bookId) { return Optional.empty(); }
            @Override public Optional<Book> findById(java.sql.Connection connection, String bookId) { return Optional.empty(); }
            @Override public java.util.List<Book> findAll() { return List.of(); }
            @Override public java.util.List<Book> search(String keyword, boolean onlyAvailable) { return List.of(); }
            @Override public Book save(Book book) { return book; }
            @Override public Book save(java.sql.Connection connection, Book book) { return book; }
            @Override public Book update(Book book) { return book; }
            @Override public Book update(java.sql.Connection connection, Book book) { return book; }
            @Override public int count() { return 0; }
        });

        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(null));
        int nextYear = LocalDate.now().getYear() + 1;
        Book future = new Book("id","t","a","p", nextYear,1,1, com.thayhoang.quanly.domain.enums.BookStatus.AVAILABLE);
        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(future));
        Book badQty = new Book("id","t","a","p",2020,1,2, com.thayhoang.quanly.domain.enums.BookStatus.AVAILABLE);
        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(badQty));
    }
}
