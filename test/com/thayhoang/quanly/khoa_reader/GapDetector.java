package com.thayhoang.quanly.khoa_reader;

import com.thayhoang.quanly.domain.enums.ReaderStatus;
import com.thayhoang.quanly.infrastructure.repository.jdbc.JdbcReaderRepository;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTable;
import javax.swing.JTextField;

// Reflection-based gap detector. Each method either CONFIRMS or REJECTS a spec/impl gap with
// evidence (enum members, declared methods, swing component tree). If we cannot prove a gap
// programmatically, we do not let the report claim one.
public final class GapDetector {
    private GapDetector() {
    }

    // --- TC15 / ReaderStatus enum check ---

    public static boolean hasReaderStatusLocked() {
        for (ReaderStatus status : ReaderStatus.values()) {
            if ("LOCKED".equals(status.name())) {
                return true;
            }
        }
        return false;
    }

    public static String describeReaderStatusEnum() {
        return Arrays.toString(ReaderStatus.values());
    }

    // --- TC18 / repository method check ---

    public static boolean hasFindOverdueReadersMethod() {
        for (Method method : JdbcReaderRepository.class.getDeclaredMethods()) {
            if (method.getName().toLowerCase().contains("overdue")) {
                return true;
            }
        }
        return false;
    }

    public static String describeReaderRepositoryMethods() {
        List<String> names = new ArrayList<>();
        for (Method method : JdbcReaderRepository.class.getDeclaredMethods()) {
            names.add(method.getName());
        }
        return names.toString();
    }

    // --- TC17 / swing component check ---

    public static int countJTextFieldsIn(Container container) {
        int count = 0;
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField) {
                count++;
            }
            if (component instanceof Container child) {
                count += countJTextFieldsIn(child);
            }
        }
        return count;
    }

    public static boolean tableHasRowSorter(JTable table) {
        return table != null && table.getRowSorter() != null;
    }
}
