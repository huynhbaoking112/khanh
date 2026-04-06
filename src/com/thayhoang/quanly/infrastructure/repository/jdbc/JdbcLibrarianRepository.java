package com.thayhoang.quanly.infrastructure.repository.jdbc;

import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.domain.enums.UserRole;
import com.thayhoang.quanly.domain.model.Librarian;
import com.thayhoang.quanly.infrastructure.db.DatabaseSupport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcLibrarianRepository implements LibrarianRepository {
    @Override
    public Optional<Librarian> findById(String librarianId) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection()) {
            return findById(connection, librarianId);
        }
    }

    @Override
    public Optional<Librarian> findById(Connection connection, String librarianId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT librarian_id, full_name, username, password, role
                FROM LIBRARIAN
                WHERE librarian_id = ?
                """)) {
            statement.setString(1, librarianId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Librarian> findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT librarian_id, full_name, username, password, role
                        FROM LIBRARIAN
                        WHERE username = ?
                        """)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Librarian> findAll() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT librarian_id, full_name, username, password, role
                        FROM LIBRARIAN
                        ORDER BY full_name
                        """);
                ResultSet resultSet = statement.executeQuery()) {
            List<Librarian> librarians = new ArrayList<>();
            while (resultSet.next()) {
                librarians.add(mapRow(resultSet));
            }
            return List.copyOf(librarians);
        }
    }

    @Override
    public Librarian save(Librarian librarian) throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO LIBRARIAN (librarian_id, full_name, username, password, role)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, librarian.librarianId());
            statement.setString(2, librarian.fullName());
            statement.setString(3, librarian.username());
            statement.setString(4, librarian.password());
            statement.setString(5, librarian.role().name());
            statement.executeUpdate();
            return librarian;
        }
    }

    @Override
    public int count() throws SQLException {
        try (Connection connection = DatabaseSupport.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM LIBRARIAN");
                ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private Librarian mapRow(ResultSet resultSet) throws SQLException {
        return new Librarian(
                resultSet.getString("librarian_id"),
                resultSet.getString("full_name"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                UserRole.valueOf(resultSet.getString("role")));
    }
}
