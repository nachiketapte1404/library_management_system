package com.nachiket.library_management.model;

public class BookInventoryDto {
    private String isbn;
    private String title;
    private String author;
    private String type;
    private String extraField; // Will hold genre, subject, or issue number
    private int totalCopies;
    private int availableCopies;

    public BookInventoryDto(String isbn, String title, String author, String type, String extraField) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.type = type;
        this.extraField = extraField;
        this.totalCopies = 0;
        this.availableCopies = 0;
    }

    public void incrementTotal() {
        this.totalCopies++;
    }

    public void incrementAvailable() {
        this.availableCopies++;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getType() {
        return type;
    }

    public String getExtraField() {
        return extraField;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public int getBorrowedCopies() {
        return totalCopies - availableCopies;
    }
}