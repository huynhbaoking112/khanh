package com.thayhoang.quanly.testsupport;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public final class AutoQcReportExtension implements TestWatcher {
    private static final String MANUAL_QC_MARKER = "QcReport.tc(";

    @Override
    public void testSuccessful(ExtensionContext context) {
        if (!hasManualQcReport(context)) {
            report(context).pass();
        }
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        if (!hasManualQcReport(context)) {
            report(context).fail(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        if (!hasManualQcReport(context)) {
            report(context).skipped(cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        if (!hasManualQcReport(context)) {
            report(context).skipped(reason.orElse("Test disabled"));
        }
    }

    private static QcReport report(ExtensionContext context) {
        String className = context.getRequiredTestClass().getSimpleName();
        String methodName = context.getRequiredTestMethod().getName();
        return QcReport.tc("AUTO-" + className + "." + methodName, inferLevel(context))
                .requirement("Auto report for JUnit test without manual QcReport entry")
                .dataset("N/A")
                .precondition("Test class: " + context.getRequiredTestClass().getName())
                .input("Run JUnit method: " + methodName)
                .expected("JUnit assertion result matches the test expectation")
                .actual("JUnit status for display name: " + context.getDisplayName());
    }

    private static String inferLevel(ExtensionContext context) {
        String packageName = context.getRequiredTestClass().getPackageName();
        if (packageName.endsWith(".ui")) {
            return "UI";
        }
        if (packageName.contains(".infrastructure.")) {
            return "Infrastructure";
        }
        if (packageName.contains(".domain.")) {
            return "Unit";
        }
        if (packageName.contains(".application.")) {
            return "Service";
        }
        return "JUnit";
    }

    private static boolean hasManualQcReport(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String className = testClass.getSimpleName();
        Path source = Path.of("test", testClass.getPackageName().replace('.', '/'), className + ".java");
        try {
            if (!Files.exists(source)) {
                return false;
            }
            String content = Files.readString(source, StandardCharsets.UTF_8);
            int methodIndex = content.indexOf(testMethod.getName() + "(");
            int markerIndex = content.indexOf(MANUAL_QC_MARKER);
            return markerIndex >= 0 && (methodIndex < 0 || markerIndex >= methodIndex || content.substring(0, methodIndex).contains(MANUAL_QC_MARKER));
        } catch (Exception ignored) {
            return false;
        }
    }
}
