package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.domain.model.Reader;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcReaderRepository implements ReaderRepository {
    @Override
    public Optional<Reader> findById(String readerId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findById(connection, readerId);
        }
    }

    @Override
    public Optional<Reader> findById(Connection connection, String readerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT reader_id, full_name, phone, email, max_borrow, status
                FROM READER
                WHERE reader_id = ?
                """)) {
            statement.setString(1, readerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Reader> findAll() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT reader_id, full_name, phone, email, max_borrow, status
                        FROM READER
                        ORDER BY full_name
                        """);
                ResultSet resultSet = statement.executeQuery()) {
            List<Reader> readers = new ArrayList<>();
            while (resultSet.next()) {
                readers.add(mapRow(resultSet));
            }
            return List.copyOf(readers);
        }
    }

    @Override
    public Reader save(Reader reader) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO READER (reader_id, full_name, phone, email, max_borrow, status)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """)) {
            bind(statement, reader, false);
            statement.executeUpdate();
            return reader;
        }
    }

    @Override
    public Reader update(Reader reader) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        UPDATE READER
                        SET full_name = ?, phone = ?, email = ?, max_borrow = ?, status = ?
                        WHERE reader_id = ?
                        """)) {
            bind(statement, reader, true);
            statement.executeUpdate();
            return reader;
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM READER");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void bind(PreparedStatement statement, Reader reader, boolean forUpdate) throws SQLException {
        int index = 1;
        if (!forUpdate) {
            statement.setString(index++, reader.readerId());
        }
        statement.setString(index++, reader.fullName());
        statement.setString(index++, reader.phone());
        statement.setString(index++, reader.email());
        statement.setInt(index++, reader.maxBorrow());
        statement.setString(index++, reader.status().name());
        if (forUpdate) {
            statement.setString(index, reader.readerId());
        }
    }

    private Reader mapRow(ResultSet resultSet) throws SQLException {
        return new Reader(
                resultSet.getString("reader_id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getInt("max_borrow"),
                ReaderStatus.valueOf(resultSet.getString("status")));
    }
}
