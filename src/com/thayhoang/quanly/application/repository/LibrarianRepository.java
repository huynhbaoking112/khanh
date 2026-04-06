package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.Librarian;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface LibrarianRepository {
    Optional<Librarian> findById(String librarianId) throws SQLException;

    Optional<Librarian> findById(Connection connection, String librarianId) throws SQLException;

    Optional<Librarian> findByUsername(String username) throws SQLException;

    List<Librarian> findAll() throws SQLException;

    Librarian save(Librarian librarian) throws SQLException;

    int count() throws SQLException;
}
