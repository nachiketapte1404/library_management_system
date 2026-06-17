package com.nachiket.library_management.model;

public class FictionBook extends Book {

    private String genre;

    public FictionBook() {

    }

    public FictionBook(int id, String isbn, String title, String author, String genre) {
        super(id, title, author, isbn);
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}