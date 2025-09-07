package com.genomic.server.util;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceMonitor {
    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong totalProcessingTime = new AtomicLong(0);
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    private static final AtomicInteger maxConcurrentConnections = new AtomicInteger(0);

    public static void requestStarted() {
        int current = activeConnections.incrementAndGet();
        maxConcurrentConnections.getAndAccumulate(current, Math::max);
        totalRequests.incrementAndGet();
    }

    public static void requestCompleted(long processingTimeMs) {
        activeConnections.decrementAndGet();
        totalProcessingTime.addAndGet(processingTimeMs);
    }

    public static void printStats() {
        long requests = totalRequests.get();
        long avgTime = requests > 0 ? totalProcessingTime.get() / requests : 0;

        System.out.println("\n=== Performance Statistics ===");
        System.out.println("Total requests: " + requests);
        System.out.println("Max concurrent connections: " + maxConcurrentConnections.get());
        System.out.println("Average processing time: " + avgTime + "ms");
        System.out.println("Active connections: " + activeConnections.get());
    }
}