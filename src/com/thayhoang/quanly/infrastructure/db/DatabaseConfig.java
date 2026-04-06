package com.thayhoang.quanly.infrastructure.db;

import java.nio.file.Path;

public final class DatabaseConfig {
    private static final Path PROJECT_ROOT = Path.of(System.getProperty("user.dir"));
    private static final Path DATA_DIRECTORY = PROJECT_ROOT.resolve("data");
    private static final Path DATABASE_FILE = DATA_DIRECTORY.resolve("library.db");
    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_FILE.toAbsolutePath();

    private DatabaseConfig() {
    }

    public static Path dataDirectory() {
        return DATA_DIRECTORY;
    }

    public static Path databaseFile() {
        return DATABASE_FILE;
    }

    public static String jdbcUrl() {
        return JDBC_URL;
    }
}
