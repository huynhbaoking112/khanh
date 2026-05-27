package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.domain.model.Librarian;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceImplTest {

    @Test
    public void login_blank_or_missing_returnsEmpty() {
        var repo = com.thayhoang.quanly.util.TestStubs.librarianRepoEmpty();
        var service = new AuthenticationServiceImpl(repo);
        assertTrue(service.login("", "").isEmpty());
        assertTrue(service.login("user", "").isEmpty());
    }

    @Test
    public void login_wrongPassword_and_success() throws SQLException {
        Librarian librarian = new Librarian("L1", "Name", "user", "pass", com.thayhoang.quanly.domain.enums.UserRole.LIBRARIAN);
        var repo = new com.thayhoang.quanly.util.TestStubs.LibrarianRepoStub() {
            @Override public Optional<Librarian> findByUsername(String username) throws SQLException { return Optional.of(librarian); }
        };
        var service = new AuthenticationServiceImpl(repo);
        assertTrue(service.login("user", "bad").isEmpty());
        assertTrue(service.login("user", "pass").isPresent());
    }

    @Test
    public void login_sqlException_throws() {
        var repo = new com.thayhoang.quanly.util.TestStubs.LibrarianRepoStub() {
            @Override public Optional<Librarian> findByUsername(String username) throws SQLException { throw new SQLException("db"); }
        };
        var service = new AuthenticationServiceImpl(repo);
        assertThrows(ApplicationException.class, () -> service.login("user", "pass"));
    }
}
