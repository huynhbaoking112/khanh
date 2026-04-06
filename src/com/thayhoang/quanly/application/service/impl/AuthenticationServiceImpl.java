package com.thayhoang.quanly.application.service.impl;

import com.thayhoang.quanly.application.exception.ApplicationException;
import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.service.AuthenticationService;
import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import com.thayhoang.quanly.domain.model.Librarian;
import java.sql.SQLException;
import java.util.Optional;

public final class AuthenticationServiceImpl implements AuthenticationService {
    private final LibrarianRepository librarianRepository;

    public AuthenticationServiceImpl(LibrarianRepository librarianRepository) {
        this.librarianRepository = librarianRepository;
    }

    @Override
    public Optional<AuthenticatedSession> login(String username, String password) {
        try {
            Optional<Librarian> librarian = librarianRepository.findByUsername(normalize(username));
            if (librarian.isEmpty()) {
                return Optional.empty();
            }

            Librarian matched = librarian.get();
            if (!matched.password().equals(password)) {
                return Optional.empty();
            }

            return Optional.of(new AuthenticatedSession(
                    matched.librarianId(),
                    matched.fullName(),
                    matched.username(),
                    matched.role()));
        } catch (SQLException exception) {
            throw new ApplicationException("Khong the xac thuc tai khoan", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
