package com.thayhoang.quanly.khoa_reader;

import java.util.ArrayList;
import java.util.List;

// Fluent builder that turns runtime evidence into a multi-line, easy-to-scan GAP / BUG block:
//
//   [LABEL]
//     key1          : value1
//     key2          : value2
//   CONCLUSION:
//     ...
//   FIX SUGGESTION:
//     ...
//   SEVERITY: MEDIUM
//
// Values are passed in by the test, so the evidence is always live data (not hardcoded prose).
public final class GapReportBuilder {
    private static final String INDENT = "    ";

    private final String evidenceLabel;
    private final List<String[]> fields = new ArrayList<>();
    private String conclusion = "";
    private String fixSuggestion = "";
    private String severity = "";

    private GapReportBuilder(String evidenceLabel) {
        this.evidenceLabel = evidenceLabel;
    }

    public static GapReportBuilder evidence(String label) {
        return new GapReportBuilder(label);
    }

    public GapReportBuilder field(String key, Object value) {
        fields.add(new String[] {key, String.valueOf(value)});
        return this;
    }

    public GapReportBuilder conclusion(String text) {
        this.conclusion = text;
        return this;
    }

    public GapReportBuilder fixSuggestion(String text) {
        this.fixSuggestion = text;
        return this;
    }

    public GapReportBuilder severity(String text) {
        this.severity = text;
        return this;
    }

    public String build() {
        int maxKeyLen = 0;
        for (String[] field : fields) {
            maxKeyLen = Math.max(maxKeyLen, field[0].length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("  [").append(evidenceLabel).append("]").append(System.lineSeparator());
        for (String[] field : fields) {
            sb.append(INDENT)
                    .append(String.format("%-" + maxKeyLen + "s", field[0]))
                    .append(" : ")
                    .append(field[1])
                    .append(System.lineSeparator());
        }
        if (!conclusion.isEmpty()) {
            sb.append("  CONCLUSION:").append(System.lineSeparator());
            sb.append(INDENT).append(conclusion).append(System.lineSeparator());
        }
        if (!fixSuggestion.isEmpty()) {
            sb.append("  FIX SUGGESTION:").append(System.lineSeparator());
            sb.append(INDENT).append(fixSuggestion).append(System.lineSeparator());
        }
        if (!severity.isEmpty()) {
            sb.append("  SEVERITY: ").append(severity);
        }
        return sb.toString();
    }
}
