package com.nachiket.library_management.repository;

import com.nachiket.library_management.model.AcademicBook;
import com.nachiket.library_management.model.Book;
import com.nachiket.library_management.model.FictionBook;
import com.nachiket.library_management.model.Magazine;

import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileBookRepository {
    private static final String books_records_file_name = "books.txt";

    public void saveBooks(List<Book> books) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(books_records_file_name))) {
            for (Book book : books) {
                String type = "GENERAL";
                String extraField = null;
                if (book instanceof FictionBook) {
                    type = "FICTION";
                    extraField = ((FictionBook) book).getGenre();
                } else if (book instanceof AcademicBook) {
                    type = "ACADEMIC";
                    extraField = ((AcademicBook) book).getSubject();
                } else if (book instanceof Magazine) {
                    type = "MAGAZINE";
                    extraField = ((Magazine) book).getIssueNumber();
                }
                writer.write(
                        book.getBookId() + "," +
                                type + "," +
                                book.getIsbn() + "," +
                                book.getTitle() + "," +
                                book.getAuthor() + "," +
                                book.isAvailable() + "," +
                                book.getIssuedToUserId() + "," +
                                extraField);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        File file = new File(books_records_file_name);
        if (!file.exists()) {
            return books;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 7)
                    continue;
                int id = Integer.parseInt(data[0]);
                String type = data[1];
                String isbn = data[2];
                String title = data[3];
                String author = data[4];
                boolean availability = Boolean.parseBoolean(data[5]);
                Integer issuedToUserId = null;
                if (!("null".equals(data[6])))
                    issuedToUserId = Integer.parseInt(data[6]);
                String extraField = (data.length > 7 && !"null".equalsIgnoreCase(data[7])) ? data[7] : "N/A";
                Book book;
                if ("FICTION".equals(type)) {
                    book = new FictionBook(id, isbn, title, author, extraField);
                } else if ("ACADEMIC".equals(type)) {
                    book = new AcademicBook(id, isbn, title, author, extraField);
                } else if ("MAGAZINE".equals(type)) {
                    book = new Magazine(id, isbn, title, author, extraField);
                } else {
                    book = new Book(id, isbn, title, author);
                }
                book.setAvailable(availability);
                book.setIssuedToUserId(issuedToUserId);
                books.add(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return books;
    }
}