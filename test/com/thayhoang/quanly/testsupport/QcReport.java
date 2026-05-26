package com.thayhoang.quanly.testsupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class QcReport {
    private static final String BAR_TOP = "==============================================================================";
    private static final String BAR_MID = "------------------------------------------------------------------------------";

    private static final Path REPORT_FILE = initReportFile("qc_report_"
            + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt");
    private static final Path LATEST_FILE = initReportFile("qc_report_latest.txt");

    private static Path initReportFile(String fileName) {
        try {
            Path folder = Paths.get("test", "report");
            Files.createDirectories(folder);
            Path file = folder.resolve(fileName);
            String header = BAR_TOP + System.lineSeparator()
                    + "QC REPORT - Library Management Test Suite" + System.lineSeparator()
                    + "Generated: " + new Date() + System.lineSeparator()
                    + "Run from: " + Paths.get("").toAbsolutePath() + System.lineSeparator()
                    + BAR_TOP + System.lineSeparator() + System.lineSeparator();
            Files.writeString(file, header, StandardCharsets.UTF_8);
            return file;
        } catch (IOException exception) {
            System.err.println("[QcReport] Cannot init report file " + fileName + ": " + exception.getMessage());
            return null;
        }
    }

    private static void appendToReportFiles(String content) {
        for (Path target : new Path[] {REPORT_FILE, LATEST_FILE}) {
            if (target == null) {
                continue;
            }
            try {
                Files.writeString(target, content, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException ignored) {
            }
        }
    }

    private final String tcId;
    private final String level;
    private String requirement = "";
    private String dataset = "";
    private String precondition = "";
    private String input = "";
    private String expected = "";
    private String actual = "";
    private String dbBefore = "";
    private String dbAfter = "";

    private QcReport(String tcId, String level) {
        this.tcId = tcId;
        this.level = level;
    }

    public static QcReport tc(String tcId, String level) {
        return new QcReport(tcId, level);
    }

    public QcReport requirement(String requirement) {
        this.requirement = requirement;
        return this;
    }

    public QcReport dataset(String dataset) {
        this.dataset = dataset;
        return this;
    }

    public QcReport precondition(String precondition) {
        this.precondition = precondition;
        return this;
    }

    public QcReport input(String input) {
        this.input = input;
        return this;
    }

    public QcReport expected(String expected) {
        this.expected = expected;
        return this;
    }

    public QcReport actual(String actual) {
        this.actual = actual;
        return this;
    }

    public QcReport dbBefore(String dbBefore) {
        this.dbBefore = dbBefore;
        return this;
    }

    public QcReport dbAfter(String dbAfter) {
        this.dbAfter = dbAfter;
        return this;
    }

    public void pass() {
        print("PASS", "");
    }

    public void gap(String gapNote) {
        print("PASS / GAP", gapNote);
    }

    private void print(String status, String gapNote) {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append(BAR_TOP).append(System.lineSeparator());
        sb.append(String.format("[%s] %s  |  Level: %s  |  Requirement: %s  |  Dataset: %s%n",
                status, tcId, level, requirement, dataset));
        sb.append(BAR_MID).append(System.lineSeparator());
        sb.append("PRECONDITION : ").append(precondition).append(System.lineSeparator());
        sb.append("INPUT        : ").append(input).append(System.lineSeparator());
        sb.append("EXPECTED     : ").append(expected).append(System.lineSeparator());
        sb.append("ACTUAL       : ").append(actual).append(System.lineSeparator());
        if (!dbBefore.isEmpty() || !dbAfter.isEmpty()) {
            sb.append(BAR_MID).append(System.lineSeparator());
            sb.append("DB BEFORE    : ").append(dbBefore).append(System.lineSeparator());
            sb.append("DB AFTER     : ").append(dbAfter).append(System.lineSeparator());
        }
        if (gapNote != null && !gapNote.isEmpty()) {
            sb.append(BAR_MID).append(System.lineSeparator());
            if (gapNote.startsWith(System.lineSeparator()) || gapNote.startsWith("\n")) {
                sb.append("GAP / BUG    :").append(gapNote);
                if (!gapNote.endsWith(System.lineSeparator())) {
                    sb.append(System.lineSeparator());
                }
            } else {
                sb.append("GAP / BUG    : ").append(gapNote).append(System.lineSeparator());
            }
        }
        sb.append(BAR_TOP).append(System.lineSeparator());
        String rendered = sb.toString();
        System.out.println(rendered);
        appendToReportFiles(rendered);
    }
}