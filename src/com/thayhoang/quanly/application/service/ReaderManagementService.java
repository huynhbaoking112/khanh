package com.thayhoang.quanly.application.service;

import com.thayhoang.quanly.domain.model.Reader;
import java.util.List;
import java.util.Optional;

public interface ReaderManagementService {
    List<Reader> listReaders();

    Optional<Reader> getReader(String readerId);

    Reader createReader(Reader reader);

    Reader updateReader(Reader reader);

    int getCurrentBorrowCount(String readerId);
}
