package com.genomic.server.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_FILE = "server-module/src/main/resources/logs/server.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warning(String message) {
        log("WARNING", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Exception e) {
        log("ERROR", message + " - " + e.getMessage());
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String logMessage = String.format("[%s] [%s] [Thread-%s] %s",
                timestamp, level, Thread.currentThread().getName(), message);

        // Console output
        System.out.println(logMessage);

        // File output
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public static void logOperation(String operation, String patientId, String details) {
        String message = String.format("Operation: %s, Patient: %s, Details: %s", operation, patientId, details);
        info(message);
    }

    public static void logDiseaseDetection(String patientId, String diseaseId, double similarity) {
        String message = String.format("Disease detected - Patient: %s, Disease: %s, Similarity: %.2f",
                patientId, diseaseId, similarity);
        info(message);
    }
}