package com.thayhoang.quanly.domain.model;

import com.thayhoang.quanly.domain.enums.ReaderStatus;

public record Reader(
        String readerId,
        String fullName,
        String phone,
        String email,
        int maxBorrow,
        ReaderStatus status) {
}
