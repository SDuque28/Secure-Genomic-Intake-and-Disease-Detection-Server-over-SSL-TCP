package com.genomic.server.util;

public class Pacient {
    private String fullName;
    private int document_id;
    private int age;
    private char gender;
    private String contact_email;

    public Pacient(int document_id, String fullName, int age, char gender, String contact_email) {
        this.document_id = document_id;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.contact_email = contact_email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    public void mostrorInfo(){
        System.out.println("Full name: " + fullName + "\nAge: " + age + "\nGender: " + gender);
    }
}
