package com.nachiket.library_management.model;

public class Book {
    protected int bookId;
    protected String title;
    protected String author;
    protected String isbn;
    protected boolean available;
    private Integer issuedToUserId;

    public Book() {
    }

    public Book(int bookId, String isbn, String title, String author) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
        this.issuedToUserId = null;
    }

    public int getBookId() {
        return this.bookId;
    }

    public void setBookId(int bookdId) {
        this.bookId = bookdId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isAvailable() {
        return this.available;
    }

    public void setAvailable(boolean availability) {
        this.available = availability;
    }

    public Integer getIssuedToUserId() {
        return issuedToUserId;
    }

    public void setIssuedToUserId(Integer issuedToUserId) {
        this.issuedToUserId = issuedToUserId;
    }

    public String getType() {
        if (this instanceof FictionBook)
            return "FICTION";
        if (this instanceof AcademicBook)
            return "ACADEMIC";
        if (this instanceof Magazine)
            return "MAGAZINE";
        return "GENERAL";
    }
}