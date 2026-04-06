package com.thayhoang.quanly.application.support;

import java.util.UUID;

public final class IdentifierGenerator {
    private IdentifierGenerator() {
    }

    public static String next(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
