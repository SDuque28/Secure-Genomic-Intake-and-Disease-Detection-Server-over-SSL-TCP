package com.genomic.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter @Getter
public class Patient {
    // Getters and Setters
    private String patientId;
    private String fullName;
    private String documentId;
    private int age;
    private String sex; // "M" or "F"
    private String email;
    private Date registrationDate;
    private String clinicalNotes;
    private String checksumFasta;
    private long fileSizeBytes;
    private boolean active;
    private String fastaFilename; // Store FASTA filename for retrieval

    // Constructors
    public Patient() {
        this.registrationDate = new Date();
        this.active = true;
    }

    public Patient(String fullName, String documentId, int age, String sex,
                   String email, String clinicalNotes, String checksumFasta,
                   long fileSizeBytes) {
        this();
        this.fullName = fullName;
        this.documentId = documentId;
        this.age = age;
        this.sex = sex;
        this.email = email;
        this.clinicalNotes = clinicalNotes;
        this.checksumFasta = checksumFasta;
        this.fileSizeBytes = fileSizeBytes;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}