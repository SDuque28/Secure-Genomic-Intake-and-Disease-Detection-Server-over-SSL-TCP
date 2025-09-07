package com.genomic.common.model;

import lombok.Getter;

/**
 * DiseaseMatchResult - Represents the result of a genomic sequence comparison
 * Contains information about a detected disease match including similarity score
 * and generated description for reporting purposes
 * Getter class - once created, the match results cannot be modified
 */
@Getter
public class DiseaseMatchResult {
    private final Disease disease;
    private final double similarity;
    private final String description;

    /**
     * Constructs a new DiseaseMatchResult with the given disease and similarity score
     * Automatically generates a descriptive message for reporting
     * @param disease the disease that was matched
     * @param similarity the similarity score (0.0 to 1.0)
     * @throws IllegalArgumentException if disease is null or similarity is out of range
     */
    public DiseaseMatchResult(Disease disease, double similarity) {
        this.disease = disease;
        this.similarity = similarity;
        this.description = generateDescription(disease, similarity);
    }

    /**
     * Generates a descriptive message for the disease match
     * Includes similarity percentage and disease severity information
     * @param disease the matched disease
     * @param similarity the similarity score
     * @return formatted description string
     */
    private String generateDescription(Disease disease, double similarity) {
        return String.format("Genomic similarity (%.2f%%) detected with %s (Severity: %d/10)",
                similarity * 100, disease.getName(), disease.getSeverity());
    }

    /**
     * Returns a string representation of the match result for debugging
     * @return string containing disease ID, similarity score, and description
     */
    @Override
    public String toString() {
        return String.format("DiseaseMatchResult{diseaseId=%s, similarity=%.2f, description=%s}",
                disease.getDiseaseId(), similarity, description);
    }
}