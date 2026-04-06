package com.thayhoang.quanly.application.service;

import com.thayhoang.quanly.domain.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookCatalogService {
    List<Book> listBooks();

    List<Book> searchBooks(String keyword, boolean onlyAvailable);

    Optional<Book> getBook(String bookId);

    Book createBook(Book book);

    Book updateBook(Book book);
}
