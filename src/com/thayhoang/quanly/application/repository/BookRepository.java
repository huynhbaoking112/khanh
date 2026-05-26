package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.Book;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Optional<Book> findById(String bookId) throws SQLException;

    Optional<Book> findById(Connection connection, String bookId) throws SQLException;

    List<Book> findAll() throws SQLException;

    List<Book> search(String keyword, boolean onlyAvailable) throws SQLException;

    Book save(Book book) throws SQLException;

    Book save(Connection connection, Book book) throws SQLException;

    Book update(Book book) throws SQLException;

    Book update(Connection connection, Book book) throws SQLException;

    int count() throws SQLException;
}
