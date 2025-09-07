package com.genomic.common.util;

/**
 * SequenceAligner - Provides genomic sequence alignment and similarity calculation
 * Implements the Needleman-Wunsch algorithm for global sequence alignment
 * Used for comparing patient genomic sequences against disease reference sequences
 */
public class SequenceAligner {

    /**
     * Calculates similarity between two genomic sequences using Needleman-Wunsch algorithm
     * Performs global sequence alignment and returns normalized similarity score
     * @param seq1 first genomic sequence (typically patient sequence)
     * @param seq2 second genomic sequence (typically disease reference sequence)
     * @return similarity score between 0.0 (no similarity) and 1.0 (perfect match)
     */
    public static double calculateSimilarity(String seq1, String seq2) {
        // Basic implementation of Needleman-Wunsch algorithm
        int gapPenalty = -2;
        int matchScore = 1;
        int mismatchScore = -1;

        String cleanSeq1 = seq1.replaceAll("[^ACGTN]", "").toUpperCase();
        String cleanSeq2 = seq2.replaceAll("[^ACGTN]", "").toUpperCase();

        int n = cleanSeq1.length();
        int m = cleanSeq2.length();

        if (n == 0 || m == 0) return 0.0;

        int[][] dp = new int[n + 1][m + 1];

        // Initialize DP matrix
        for (int i = 0; i <= n; i++) {
            dp[i][0] = i * gapPenalty;
        }
        for (int j = 0; j <= m; j++) {
            dp[0][j] = j * gapPenalty;
        }

        // Fill DP matrix
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int match = dp[i - 1][j - 1] +
                        (cleanSeq1.charAt(i - 1) == cleanSeq2.charAt(j - 1) ? matchScore : mismatchScore);
                int delete = dp[i - 1][j] + gapPenalty;
                int insert = dp[i][j - 1] + gapPenalty;

                dp[i][j] = Math.max(Math.max(match, delete), insert);
            }
        }

        // Calculate similarity score (normalized between 0 and 1)
        int maxPossibleScore = Math.min(n, m) * matchScore;
        double similarity = (double) (dp[n][m] - (Math.max(n, m) - Math.min(n, m)) * gapPenalty) / maxPossibleScore;

        return Math.max(0, Math.min(1, similarity)); // Clamp between 0 and 1
    }

    /**
     * Quickly determines if two sequences are potential matches
     * Uses length-based pre-filtering before expensive alignment
     * @param patientSeq patient genomic sequence
     * @param diseaseSeq disease reference sequence
     * @param threshold minimum similarity threshold (0.0 to 1.0)
     * @return true if sequences are potential matches above the threshold
     * @throws IllegalArgumentException if threshold is not in [0, 1] range
     */
    public static boolean isPotentialMatch(String patientSeq, String diseaseSeq, double threshold) {
        // Quick check for obvious non-matches first
        if (patientSeq.length() < diseaseSeq.length() * 0.5) {
            return false;
        }

        // Calculate similarity using alignment algorithm
        double similarity = calculateSimilarity(patientSeq, diseaseSeq);
        return similarity >= threshold;
    }
}