package com.thayhoang.quanly.khanh_book;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BookCatalogServiceImplTest {
    @Test
    void createBook_rejectsFutureYear() throws Exception {
        BookRepository repo = mock(BookRepository.class);
        when(repo.findById("BKFUT")).thenReturn(Optional.empty());
        BookCatalogServiceImpl service = new BookCatalogServiceImpl(repo);

        int futureYear = java.time.LocalDate.now().getYear() + 1;
        Book future = new Book("BKFUT", "Future Book", "Author", "Pub", futureYear, 1, 1, BookStatus.AVAILABLE);

        assertThrows(BusinessRuleViolationException.class, () -> service.createBook(future));
    }
}
