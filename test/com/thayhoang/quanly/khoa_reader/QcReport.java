package com.thayhoang.quanly.khoa_reader;

// QC-style test reporter for Khoa's Reader Management testcases (TC13-TC18).
// Each test calls QcReport.tc(...).input(...).expected(...).actual(...).pass()/gap() to print
// a structured "QC report card" to the terminal during `mvn test`.
public final class QcReport {
    private static final String BAR_TOP = "==============================================================================";
    private static final String BAR_MID = "------------------------------------------------------------------------------";

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
            sb.append("GAP / BUG    : ").append(gapNote).append(System.lineSeparator());
        }
        sb.append(BAR_TOP).append(System.lineSeparator());
        System.out.println(sb.toString());
    }
}
