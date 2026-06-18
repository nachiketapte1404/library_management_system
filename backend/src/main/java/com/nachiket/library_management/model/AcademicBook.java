package com.nachiket.library_management.model;

public class AcademicBook extends Book {
    private String subject;

    public AcademicBook() {
        super();
    }

    public AcademicBook(int id, String isbn, String title, String author, String subject) {
        super(id, isbn, title, author);
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}