package com.thayhoang.quanly.ui;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class BookPanelRunSafelyExceptionTest {

    @Test
    public void runSafely_swallowRuntimeExceptions_and_notThrow() throws Exception {
        Method run = LibraryShellFrame.class.getDeclaredMethod("runSafely", Runnable.class);
        run.setAccessible(true);

        Runnable r = () -> { throw new RuntimeException("boom"); };

        assertDoesNotThrow(() -> run.invoke(null, r));
    }
}
