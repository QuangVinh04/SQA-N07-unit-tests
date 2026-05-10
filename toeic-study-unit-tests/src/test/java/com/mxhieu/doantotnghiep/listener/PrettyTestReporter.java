package com.mxhieu.doantotnghiep.listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PrettyTestReporter implements TestExecutionListener {

    static {
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
    }

    private final Map<String, List<Row>> resultsByClass = new LinkedHashMap<>();

    private static class Row {
        String status;
        String tcId;
        int tcNumber; // số cuối trong TC-XX-01 để sort

        Row(String status, String tcId) {
            this.status = status;
            this.tcId = tcId;
            // Tách số cuối: "TC-LP-01" -> 1
            try {
                String[] parts = tcId.split("-");
                this.tcNumber = Integer.parseInt(parts[parts.length - 1]);
            } catch (Exception e) {
                this.tcNumber = 999;
            }
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
        if (!testIdentifier.isTest()) return;

        String displayName = testIdentifier.getDisplayName();

        // Lấy class name
        String className = testIdentifier.getParentId()
                .flatMap(pid -> Optional.of(pid
                        .replaceAll(".*\\[class:", "")
                        .replaceAll("\\].*", "")
                        .replaceAll(".*\\.", "")))
                .orElse("Unknown");

        // Tách TC ID từ DisplayName
        String tcId = "-";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(TC-[A-Z]+-\\d+)")
                .matcher(displayName);
        if (m.find()) {
            tcId = m.group(1).trim();
        }

        String status;
        switch (result.getStatus()) {
            case SUCCESSFUL -> status = "PASS";
            case FAILED     -> status = "FAIL";
            default         -> status = "ERR ";
        }

        resultsByClass.computeIfAbsent(className, k -> new ArrayList<>())
                      .add(new Row(status, tcId));
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println();

        int grandTotal = 0, grandPass = 0, grandFail = 0;

        for (Map.Entry<String, List<Row>> entry : resultsByClass.entrySet()) {
            String className = entry.getKey();
            List<Row> rows = entry.getValue();

            // Sắp xếp theo số TC
            rows.sort(Comparator.comparingInt(r -> r.tcNumber));

            // Đánh lại STT sau khi sort
            int pass = 0, fail = 0;
            for (Row r : rows) {
                if (r.status.equals("PASS")) pass++; else fail++;
            }
            grandTotal += rows.size();
            grandPass  += pass;
            grandFail  += fail;

            String title = "CHECKLIST TEST CASE: " + className;
            System.out.println("=".repeat(50));
            System.out.printf("  %s%n", title);
            System.out.println("=".repeat(50));
            System.out.printf("%-4s | %-4s | %-15s%n", "STT", "TT", "TestCase");
            System.out.println("-".repeat(4) + "+" + "-".repeat(6) + "+" + "-".repeat(17));

            int stt = 1;
            for (Row row : rows) {
                System.out.printf("%-4d | %-4s | %-15s%n", stt++, row.status, row.tcId);
            }

            System.out.printf("%nKet qua: PASS=%d / FAIL=%d / TOTAL=%d%n%n", pass, fail, rows.size());
        }

        System.out.println("=".repeat(50));
        System.out.printf("  TONG KET: PASS=%d | FAIL=%d | TOTAL=%d%n",
                grandPass, grandFail, grandTotal);
        System.out.println("=".repeat(50));
        System.out.println();
    }
}
