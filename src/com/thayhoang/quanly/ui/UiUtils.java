package com.thayhoang.quanly.ui;

import com.thayhoang.quanly.application.exception.ApplicationException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import com.thayhoang.quanly.domain.model.Book;
import com.thayhoang.quanly.domain.enums.BookStatus;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.application.service.dto.LoanRecord;
import com.thayhoang.quanly.domain.model.LoanDetail;

public final class UiUtils {
    private UiUtils() {}

    public static int parseInt(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new ApplicationException(fieldName + " khong hop le.");
        }
    }

    public static LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException exception) {
            throw new ApplicationException(fieldName + " phai theo dinh dang yyyy-MM-dd.");
        }
    }

    public static List<String> parseCsv(String input) {
        List<String> values = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return values;
        }
        for (String token : input.split(",")) {
            String value = token.trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    public static String formatLoanReceipt(String loanId, String readerId, String librarianId, String dueDate, int detailsSize) {
        return String.format(
                "\nLap phieu muon thanh cong\nMa phieu: %s\nDoc gia: %s\nThu thu: %s\nHan tra: %s\nSo dau sach: %s\n",
                loanId, readerId, librarianId, dueDate, detailsSize);
    }

    public static String formatReturnReceipt(String loanId, String status, String returnDate, String fineText) {
        return String.format(
                "\nTra sach thanh cong\nMa phieu: %s\nTrang thai: %s\nNgay tra: %s\n%s\n",
                loanId, status, returnDate, fineText);
    }

    public static Book createBookFromFields(
            String id,
            String title,
            String author,
            String publisher,
            String yearText,
            String totalText,
            String availableText,
            String statusText) {
        int year = parseInt(yearText, "Nam xuat ban");
        int total = parseInt(totalText, "So luong tong");
        int available = parseInt(availableText, "So luong kha dung");
        BookStatus status = BookStatus.valueOf(statusText == null || statusText.isBlank() ? BookStatus.AVAILABLE.name() : statusText);
        return new Book(id == null ? "" : id.trim(),
                title == null ? "" : title.trim(),
                author == null ? "" : author.trim(),
                publisher == null ? "" : publisher.trim(),
                year,
                total,
                available,
                status);
    }

    public static Reader createReaderFromFields(
            String id,
            String fullName,
            String phone,
            String email,
            String maxBorrowText,
            String statusText) {
        int maxBorrow = parseInt(maxBorrowText, "Gioi han muon");
        ReaderStatus status = ReaderStatus.valueOf(statusText == null || statusText.isBlank() ? ReaderStatus.ACTIVE.name() : statusText);
        return new Reader(id == null ? "" : id.trim(),
                fullName == null ? "" : fullName.trim(),
                phone == null ? "" : phone.trim(),
                email == null ? "" : email.trim(),
                maxBorrow,
                status);
    }

    public static Book createBookFromRow(Object[] row) {
        String id = row[0] == null ? "" : String.valueOf(row[0]);
        String title = row[1] == null ? "" : String.valueOf(row[1]);
        String author = row[2] == null ? "" : String.valueOf(row[2]);
        String publisher = row[3] == null ? "" : String.valueOf(row[3]);
        String yearText = row[4] == null ? "0" : String.valueOf(row[4]);
        String totalText = row[5] == null ? "0" : String.valueOf(row[5]);
        String availableText = row[6] == null ? "0" : String.valueOf(row[6]);
        String statusText = row[7] == null ? BookStatus.AVAILABLE.name() : String.valueOf(row[7]);
        return createBookFromFields(id, title, author, publisher, yearText, totalText, availableText, statusText);
    }

    public static Reader createReaderFromRow(Object[] row) {
        String id = row[0] == null ? "" : String.valueOf(row[0]);
        String fullName = row[1] == null ? "" : String.valueOf(row[1]);
        String phone = row[2] == null ? "" : String.valueOf(row[2]);
        String email = row[3] == null ? "" : String.valueOf(row[3]);
        String maxBorrowText = row[4] == null ? "0" : String.valueOf(row[4]);
        String statusText = row[5] == null ? ReaderStatus.ACTIVE.name() : String.valueOf(row[5]);
        return createReaderFromFields(id, fullName, phone, email, maxBorrowText, statusText);
    }

    public static String formatLoanRecordDetails(LoanRecord loanRecord) {
        StringBuilder detail = new StringBuilder();
        detail.append("Phieu muon: ").append(loanRecord.loan().loanId()).append('\n');
        detail.append("Doc gia: ").append(loanRecord.reader().fullName()).append(" (")
                .append(loanRecord.reader().readerId()).append(")\n");
        detail.append("Thu thu: ").append(loanRecord.librarian().fullName()).append('\n');
        detail.append("Ngay muon: ").append(loanRecord.loan().loanDate()).append('\n');
        detail.append("Han tra: ").append(loanRecord.loan().dueDate()).append('\n');
        detail.append("Ngay tra: ").append(loanRecord.loan().returnDate()).append('\n');
        detail.append("Trang thai: ").append(loanRecord.loan().status().name()).append("\n\n");
        detail.append("Chi tiet sach:\n");
        for (LoanDetail item : loanRecord.details()) {
            detail.append("- ").append(item.bookId())
                    .append(" | qty=").append(item.quantity())
                    .append(" | returned=").append(item.returned())
                    .append('\n');
        }
        detail.append('\n');
        detail.append("Phat: ").append(loanRecord.fine()
                .map(f -> f.amount() + " - " + f.reason() + " - " + f.paidStatus().name())
                .orElse("Khong co"));
        return detail.toString();
    }
}
