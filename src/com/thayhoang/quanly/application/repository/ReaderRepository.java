package com.thayhoang.quanly.application.repository;

import com.thayhoang.quanly.domain.model.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ReaderRepository {
    Optional<Reader> findById(String readerId) throws SQLException;

    Optional<Reader> findById(Connection connection, String readerId) throws SQLException;

    List<Reader> findAll() throws SQLException;

    Reader save(Reader reader) throws SQLException;

    Reader update(Reader reader) throws SQLException;

    int count() throws SQLException;
}
