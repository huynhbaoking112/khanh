package com.thayhoang.quanly.king_auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.service.impl.AuthenticationServiceImpl;
import com.thayhoang.quanly.domain.model.Librarian;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AuthenticationServiceImplTest {

    @Test
    void login_withBlankCredentials_doesNotCallRepository() {
        LibrarianRepository repo = mock(LibrarianRepository.class);
        AuthenticationServiceImpl svc = new AuthenticationServiceImpl(repo);

        svc.login("   ", "");

        verifyNoInteractions(repo);
    }

    @Test
    void login_withValidCredentials_callsRepositoryAndReturnsSession() throws Exception {
        LibrarianRepository repo = mock(LibrarianRepository.class);
        AuthenticationServiceImpl svc = new AuthenticationServiceImpl(repo);

        var lib = new com.thayhoang.quanly.domain.model.Librarian("LIB1", "Admin User", "admin", "admin123", com.thayhoang.quanly.domain.enums.UserRole.ADMIN);
        when(repo.findByUsername("admin")).thenReturn(Optional.of(lib));

        var session = svc.login("admin", "admin123");
        assert(session.isPresent());
    }
}
