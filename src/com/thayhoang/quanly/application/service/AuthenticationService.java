package com.thayhoang.quanly.application.service;

import com.thayhoang.quanly.application.service.dto.AuthenticatedSession;
import java.util.Optional;

public interface AuthenticationService {
    Optional<AuthenticatedSession> login(String username, String password);
}
