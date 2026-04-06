package com.thayhoang.quanly.application.service.dto;

import com.thayhoang.quanly.domain.enums.UserRole;

public record AuthenticatedSession(
        String librarianId,
        String fullName,
        String username,
        UserRole role) {
}
