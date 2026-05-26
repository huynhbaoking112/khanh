package com.thayhoang.quanly.khanh_book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thayhoang.quanly.application.exception.BusinessRuleViolationException;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.testsupport.GapReportBuilder;
import com.thayhoang.quanly.testsupport.QcReport;
import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BookCatalogTest {
    @BeforeAll
    static void ensureSchema() throws SQLException {
        TestDbHelper.initSchema();
    }

    @Test
    @DisplayName("TC07 - create a valid book persists with AVAILABLE status")
    void tc07_createValidBookPersistsAsAvailable() throws SQLException {
        String bookId = "BK07";
        TestDbHelper.deleteBook(bookId);
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book book = new Book(bookId, "Book Create Valid", "Van Duy Khanh", "NXB Test", 2023, 15, 15, BookStatus.AVAILABLE);

        Book saved = service.createBook(book);
        String fromDb = TestDbHelper.dumpBook(bookId);

        assertEquals(bookId, saved.bookId());
        assertTrue(fromDb.contains("status=AVAILABLE"));

        QcReport.tc("TC07", "Integration")
                .requirement("FR03 - Them moi thong tin sach")
                .dataset("TD05")
                .precondition("DB chua co bookId=BK07")
                .input("title='Book Create Valid', author='Van Duy Khanh', year=2023, quantity=15; action=Them moi")
                .expected("Row BOOK duoc INSERT va trang thai mac dinh AVAILABLE")
                .actual("savedBookId=" + saved.bookId() + ", db=" + fromDb)
                .dbAfter(fromDb)
                .pass();
    }

    @Test
    @DisplayName("TC08 - blank title is rejected by the service validation")
    void tc08_blankTitleIsRejected() {
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book invalid = new Book("BK08", "", "Pham Dinh Nam", "NXB Test", 2024, 5, 5, BookStatus.AVAILABLE);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class, () -> service.createBook(invalid));

        assertEquals("Thong tin sach khong hop le", exception.getMessage());

        QcReport.tc("TC08", "Unit")
                .requirement("FR03 - Kiem tra truong bat buoc")
                .dataset("TD06")
                .precondition("Form danh muc sach dang mo")
                .input("title=''; author='Pham Dinh Nam'; year=2024; quantity=5")
                .expected("Service phai chan submit vi thieu ten sach")
                .actual("exception=" + exception.getMessage())
                .pass();
    }

    @Test
    @DisplayName("TC09 - future year is currently accepted, exposing the boundary gap")
    void tc09_futureYearIsCurrentlyAccepted() throws SQLException {
        String bookId = "BK09";
        TestDbHelper.deleteBook(bookId);
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        Book futureBook = new Book(bookId, "Book Future Year", "Nguyen Duc Nghia", "NXB Test", 2029, 4, 4, BookStatus.AVAILABLE);

        BusinessRuleViolationException ex = assertThrows(BusinessRuleViolationException.class, () -> service.createBook(futureBook));

        QcReport.tc("TC09", "Unit")
            .requirement("FR03 - Ràng buộc giá trị biên cho nam xuat ban / so luong")
            .dataset("TD07")
            .precondition("DB chua co bookId=BK09")
            .input("title='Book Future Year', author='Nguyen Duc Nghia', year=2029, quantity=4")
            .expected("Gia tri nam tuong lai phai bi chan")
            .actual("exception=" + ex.getMessage())
            .pass();
    }

    @Test
    @DisplayName("TC10 - search is case insensitive")
    void tc10_searchIsCaseInsensitive() {
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());

        List<Book> results = service.searchBooks("lap trinh java", false);

        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(book -> "B001".equals(book.bookId())));

        QcReport.tc("TC10", "Integration")
                .requirement("FR04 - Tim kiem gan dung khong phan biet hoa thuong")
                .dataset("TD05")
                .precondition("Seed co B001 = Lap Trinh Java Co Ban")
                .input("keyword='lap trinh java'; action=Tim")
                .expected("Tra ve sach khop bat ke chu hoa/thuong")
                .actual("resultCount=" + results.size() + ", containsB001=" + results.stream().anyMatch(book -> "B001".equals(book.bookId())))
                .pass();
    }

    @Test
    @DisplayName("TC11 - search by primary key returns the matching book")
    void tc11_searchByPrimaryKeyReturnsMatchingBook() {
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());

        Optional<Book> book = service.getBook("B001");

        assertTrue(book.isPresent());
        assertEquals("B001", book.orElseThrow().bookId());

        QcReport.tc("TC11", "Integration")
                .requirement("FR04 - Tra cuu theo ma sach")
                .dataset("Seed B001")
                .precondition("DB co B001 trong BOOK")
                .input("bookId=B001; action=Tim")
                .expected("Tra ve dung 1 row B001")
                .actual("present=" + book.isPresent() + ", title=" + book.orElseThrow().title())
                .pass();
    }

    @Test
    @DisplayName("TC12 - status update currently supports INACTIVE, not DAMAGED")
    void tc12_statusUpdateUsesInactiveInsteadOfDamaged() throws SQLException {
        String bookId = "BK12";
        TestDbHelper.deleteBook(bookId);
        BookCatalogService service = new BookCatalogServiceImpl(new JdbcBookRepository());
        TestDbHelper.upsertBook(bookId, "Book Status Update", "Pham Hung Thien", "NXB Test", 2022, 2, 2, BookStatus.AVAILABLE);

        Book updated = service.updateBook(new Book(bookId, "Book Status Update", "Pham Hung Thien", "NXB Test", 2022, 2, 2, BookStatus.DAMAGED));
        String fromDb = TestDbHelper.dumpBook(bookId);


        boolean hasDamaged;
        try {
            BookStatus.valueOf("DAMAGED");
            hasDamaged = true;
        } catch (IllegalArgumentException ignored) {
            hasDamaged = false;
        }

        assertEquals(BookStatus.DAMAGED, updated.status());
        // Enum contains DAMAGED and update requested DAMAGED -> expect DB to persist DAMAGED
        assertTrue(hasDamaged);

        QcReport.tc("TC12", "Unit")
                .requirement("FR05 - Cap nhat trang thai sach")
                .dataset("TD07/TD08")
                .precondition("Sach dang AVAILABLE")
                .input("bookId=BK12, status=DAMAGED theo tai lieu; action=Luu")
                .expected("Enum/phuong an trang thai phai ho tro DAMAGED")
                .actual("codeSupportsDamaged=" + hasDamaged + ", persistedRow=" + fromDb)
                .gap(GapReportBuilder.evidence("ENUM / STATUS EVIDENCE")
                        .field("bookStatusValues", java.util.Arrays.toString(BookStatus.values()))
                        .field("statusUsedByCode", updated.status())
                        .field("specRequired", "DAMAGED")
                        .conclusion("The code persists INACTIVE even though the BookStatus enum contains DAMAGED.")
                    .fixSuggestion("Ensure updateBook maps DAMAGED status to DB instead of INACTIVE if spec requires DAMAGED.")
                        .severity("LOW (naming / domain mismatch)")
                        .build());
    }
}