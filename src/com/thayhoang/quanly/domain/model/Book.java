package com.thayhoang.quanly.domain.model;

import com.thayhoang.quanly.domain.enums.BookStatus;

public record Book(
        String bookId,
        String title,
        String author,
        String publisher,
        int yearPublish,
        int quantityTotal,
        int quantityAvailable,
        BookStatus status) {
}
