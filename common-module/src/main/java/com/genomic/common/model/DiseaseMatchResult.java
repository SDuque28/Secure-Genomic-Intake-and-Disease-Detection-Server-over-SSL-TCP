package com.genomic.common.model;

public class DiseaseMatchResult {
    private final Disease disease;
    private final double similarity;
    private final String description;

    public DiseaseMatchResult(Disease disease, double similarity) {
        this.disease = disease;
        this.similarity = similarity;
        this.description = generateDescription(disease, similarity);
    }

    public Disease getDisease() {
        return disease;
    }

    public double getSimilarity() {
        return similarity;
    }

    public String getDescription() {
        return description;
    }

    private String generateDescription(Disease disease, double similarity) {
        return String.format("Genomic similarity (%.2f%%) detected with %s (Severity: %d/10)",
                similarity * 100, disease.getName(), disease.getSeverity());
    }

    @Override
    public String toString() {
        return String.format("DiseaseMatchResult{diseaseId=%s, similarity=%.2f, description=%s}",
                disease.getDiseaseId(), similarity, description);
    }
}