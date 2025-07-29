package com.triton.msa.triton_dashboard.private_data.util;

import java.util.List;

public class FileTypeUtil {

    private static final List<String> TEXT_EXT = List.of(".txt", ".log", ".env", ".ini", ".conf", ".csv", ".md", ".yaml", ".yml");
    private static final List<String> FORBIDDEN_EXT = List.of(".exe", ".sh", ".bat");

    public static boolean isPlainText(String filename) {
        return TEXT_EXT.stream().anyMatch(filename.toLowerCase()::endsWith);
    }

    public static boolean isAllowed(String filename) {
        return FORBIDDEN_EXT.stream().noneMatch(filename.toLowerCase()::endsWith);
    }

    public static String resolveContentType(String filename) {
        String lower = filename.toLowerCase();

        if (isPlainText(lower)) return "text/plain";
        if (lower.endsWith(".md")) return "text/markdown";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".yaml") || lower.endsWith(".yml")) return "application/x-yaml";
        if (lower.endsWith(".csv")) return "text/csv";
        if (lower.endsWith(".xml")) return "application/xml";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lower.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

        return "application/octet-stream";
    }
}