package com.genomic.common.model;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Disease {
    private String diseaseId;
    private String name;
    private int severity; // 1-10
    private String fastaFilename;

    // Constructors, Getters, and Setters
    public Disease() {}

    public Disease(String diseaseId, String name, int severity, String fastaFilename) {
        this.diseaseId = diseaseId;
        this.name = name;
        this.severity = severity;
        this.fastaFilename = fastaFilename;
    }

}