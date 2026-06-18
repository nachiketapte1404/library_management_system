package com.nachiket.library_management.service;

import com.nachiket.library_management.model.AcademicBook;
import com.nachiket.library_management.model.Book;
import com.nachiket.library_management.model.FictionBook;
import com.nachiket.library_management.model.Magazine;
import com.nachiket.library_management.model.User;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileManager {
    private static final String FILE_NAME = "books.txt";

    public void saveBooks(List<Book> books) {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_NAME))) {
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

    public void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt"))) {
            for (User user : users) {
                writer.write(
                        user.getUserId() + "," +
                                user.getName() + "," +
                                (user.getUniqueIdCard() != null ? user.getUniqueIdCard() : "null"));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users file data tracking: " + e.getMessage());
        }
    }

    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        File file = new File(FILE_NAME);
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
                String author = data[2];
                String isbn = data[3];
                String title = data[4];
                boolean availability = Boolean.parseBoolean(data[5]);

                Integer issuedToUserId = null;
                if (!("null".equals(data[6])))
                    issuedToUserId = Integer.parseInt(data[6]);

                // Clean extra field handling: convert literal "null" text or empty data to
                // "N/A"
                String extraField = (data.length > 7 && !"null".equalsIgnoreCase(data[7])) ? data[7] : "N/A";

                Book book;
                if ("FICTION".equals(type)) {
                    book = new FictionBook(id, isbn, title, author, extraField);
                } else if ("ACADEMIC".equals(type)) {
                    book = new AcademicBook(id, isbn, title, author, extraField);
                } else if ("MAGAZINE".equals(type)) {
                    book = new Magazine(id, isbn, title, author, extraField);
                } else {
                    book = new Book(id, title, author, isbn);
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

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File("users.txt");

        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    int id = Integer.parseInt(data[0]);
                    String name = data[1];

                    // 🚀 Guard check: Fallback to "N/A" if reading an old pre-existing data line
                    String uniqueIdCard = (data.length > 2) ? data[2] : "N/A";

                    users.add(new User(id, name, uniqueIdCard));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading users registry snapshot tracking records: " + e.getMessage());
        }
        return users;
    }
}