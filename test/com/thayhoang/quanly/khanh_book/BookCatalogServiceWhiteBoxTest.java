package com.thayhoang.quanly.khanh_book;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class BookCatalogServiceWhiteBoxTest {

    @Test
    public void validate_nullBook_throwsBusinessRule() {
        BookRepository repo = Mockito.mock(BookRepository.class);
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(null));
    }

    @Test
    public void validate_futureYear_throwsBusinessRule() {
        BookRepository repo = Mockito.mock(BookRepository.class);
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        int nextYear = LocalDate.now().getYear() + 1;
        Book book = new Book("BFX", "Future", "Author", "Pub", nextYear, 1, 1, BookStatus.AVAILABLE);

        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(book));
    }

    @Test
    public void validate_quantityAvailableGreaterThanTotal_throwsBusinessRule() {
        BookRepository repo = Mockito.mock(BookRepository.class);
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        int year = LocalDate.now().getYear();
        Book book = new Book("BQ1", "Title", "Auth", "Pub", year, 1, 2, BookStatus.AVAILABLE);

        assertThrows(BusinessRuleViolationException.class, () -> svc.createBook(book));
    }

    @Test
    public void listBooks_repositoryThrowsSQLException_wrapsInApplicationException() throws SQLException {
        BookRepository repo = Mockito.mock(BookRepository.class);
        Mockito.when(repo.findAll()).thenThrow(new SQLException("db down"));
        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        ApplicationException ex = assertThrows(ApplicationException.class, svc::listBooks);
        assertTrue(ex.getMessage().contains("Khong the tai danh muc sach"));
    }

    @Test
    public void createBook_repositorySaveThrowsSQLException_wrapsInApplicationException() throws SQLException {
        BookRepository repo = Mockito.mock(BookRepository.class);
        int year = LocalDate.now().getYear();
        Book book = new Book("BX", "T", "A", "P", year, 1, 1, BookStatus.AVAILABLE);

        Mockito.when(repo.findById("BX")).thenReturn(Optional.empty());
        Mockito.when(repo.save(book)).thenThrow(new SQLException("insert failed"));

        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        ApplicationException ex = assertThrows(ApplicationException.class, () -> svc.createBook(book));
        assertTrue(ex.getMessage().contains("Khong the tao sach moi"));
    }

    @Test
    public void updateBook_repositoryUpdateThrowsSQLException_wrapsInApplicationException() throws SQLException {
        BookRepository repo = Mockito.mock(BookRepository.class);
        int year = LocalDate.now().getYear();
        Book book = new Book("BU", "T", "A", "P", year, 1, 1, BookStatus.AVAILABLE);

        Mockito.when(repo.findById("BU")).thenReturn(Optional.of(book));
        Mockito.when(repo.update(book)).thenThrow(new SQLException("update failed"));

        BookCatalogServiceImpl svc = new BookCatalogServiceImpl(repo);

        ApplicationException ex = assertThrows(ApplicationException.class, () -> svc.updateBook(book));
        assertTrue(ex.getMessage().contains("Khong the cap nhat sach"));
    }
}
