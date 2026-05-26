package com.thayhoang.quanly.application.bootstrap;

import com.thayhoang.quanly.application.repository.BookRepository;
import com.thayhoang.quanly.application.repository.FineRepository;
import com.thayhoang.quanly.application.repository.LibrarianRepository;
import com.thayhoang.quanly.application.repository.LoanDetailRepository;
import com.thayhoang.quanly.application.repository.LoanRepository;
import com.thayhoang.quanly.application.repository.ReaderRepository;
import com.thayhoang.quanly.application.service.AuthenticationService;
import com.thayhoang.quanly.application.service.BookCatalogService;
import com.thayhoang.quanly.application.service.CirculationService;
import com.thayhoang.quanly.application.service.HistoryService;
import com.thayhoang.quanly.application.service.ReaderManagementService;
import com.thayhoang.quanly.application.service.impl.AuthenticationServiceImpl;
import com.thayhoang.quanly.application.service.impl.BookCatalogServiceImpl;
import com.thayhoang.quanly.application.service.impl.CirculationServiceImpl;
import com.thayhoang.quanly.application.service.impl.HistoryServiceImpl;
import com.thayhoang.quanly.application.service.impl.ReaderManagementServiceImpl;
import com.thayhoang.quanly.infrastructure.db.DatabaseBootstrap;
import com.thayhoang.quanly.infrastructure.db.DatabaseConfig;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcBookRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcFineRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLibrarianRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanDetailRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcLoanRepository;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import java.nio.file.Path;
import java.sql.SQLException;

public record ApplicationBootstrap(
        Path databasePath,
        AuthenticationService authenticationService,
        BookCatalogService bookCatalogService,
        ReaderManagementService readerManagementService,
        CirculationService circulationService,
        HistoryService historyService) {
    public static ApplicationBootstrap initialize() throws SQLException {
        DatabaseBootstrap.initialize();

        BookRepository bookRepository = new JdbcBookRepository();
        ReaderRepository readerRepository = new JdbcReaderRepository();
        LibrarianRepository librarianRepository = new JdbcLibrarianRepository();
        LoanRepository loanRepository = new JdbcLoanRepository();
        LoanDetailRepository loanDetailRepository = new JdbcLoanDetailRepository();
        FineRepository fineRepository = new JdbcFineRepository();

        AuthenticationService authenticationService = new AuthenticationServiceImpl(librarianRepository);
        BookCatalogService bookCatalogService = new BookCatalogServiceImpl(bookRepository);
        ReaderManagementService readerManagementService =
                new ReaderManagementServiceImpl(readerRepository, loanDetailRepository);
        CirculationService circulationService = new CirculationServiceImpl(
                readerRepository,
                librarianRepository,
                bookRepository,
                loanRepository,
                loanDetailRepository,
                fineRepository);
        HistoryService historyService = new HistoryServiceImpl(
                readerRepository,
                librarianRepository,
                loanRepository,
                loanDetailRepository,
                fineRepository);

        return new ApplicationBootstrap(
                DatabaseConfig.databaseFile(),
                authenticationService,
                bookCatalogService,
                readerManagementService,
                circulationService,
                historyService);
    }
}
