package com.thayhoang.quanly.domain.model;

import com.thayhoang.quanly.domain.enums.UserRole;

public record Librarian(
        String librarianId,
        String fullName,
        String username,
        String password,
        UserRole role) {
}
