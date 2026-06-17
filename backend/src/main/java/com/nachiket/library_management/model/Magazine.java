package com.nachiket.library_management.model;

public class Magazine extends Book {
    private String issueNumber;

    public Magazine() {
        super();
    }

    public Magazine(int id, String isbn, String title, String author, String issueNumber) {
        super(id, title, author, isbn);
        this.issueNumber = issueNumber;
    }

    public String getIssueNumber() {
        return issueNumber;
    }

    public void setIssueNumber(String issueNumber) {
        this.issueNumber = issueNumber;
    }
}