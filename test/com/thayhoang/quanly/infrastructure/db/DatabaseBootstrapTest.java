package com.thayhoang.quanly.infrastructure.db;

import com.thayhoang.quanly.testsupport.TestDbHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseBootstrapTest {

    @Test
    public void initialize_createsSchemaAndSeedsData() throws SQLException {
        DatabaseBootstrap.initialize();

        try (Connection conn = DatabaseSupport.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM LIBRARIAN");
             ResultSet rs = stmt.executeQuery()) {
            assertTrue(rs.next());
            assertTrue(rs.getInt(1) >= 1);
        }
    }
}
