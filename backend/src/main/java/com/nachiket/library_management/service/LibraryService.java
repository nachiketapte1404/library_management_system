package com.nachiket.library_management.service;

import com.nachiket.library_management.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LibraryService {

    private final FileManager fileManager;
    private List<Book> books;
    private List<User> users;
    private int nextBookId = 1;

    public LibraryService(FileManager fileManager) {
        this.fileManager = fileManager;
        books = fileManager.loadBooks();
        users = fileManager.loadUsers();
        for (Book book : books) {
            if (book.getBookId() >= nextBookId) {
                nextBookId = book.getBookId() + 1;
            }
        }
    }

    public Book findBookByIsbn(String isbn) {
        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn))
                return book;
        }
        return null;
    }

    public void addBooksBatch(Book templateBook, int quantity) {
        for (int i = 0; i < quantity; i++) {
            Book newCopy = new Book();
            if (templateBook instanceof FictionBook) {
                FictionBook fictionCopy = new FictionBook();
                String genre = ((FictionBook) templateBook).getGenre();
                fictionCopy.setGenre(genre);
                newCopy = fictionCopy;
            } else if (templateBook instanceof com.nachiket.library_management.model.AcademicBook) {
                AcademicBook academicCopy = new AcademicBook();
                academicCopy.setSubject(((AcademicBook) templateBook).getSubject());
                newCopy = academicCopy;
            } else if (templateBook instanceof com.nachiket.library_management.model.Magazine) {
                Magazine magazineCopy = new Magazine();
                magazineCopy.setIssueNumber(((Magazine) templateBook).getIssueNumber());
                newCopy = magazineCopy;
            }
            newCopy.setBookId(nextBookId++);
            newCopy.setIsbn(templateBook.getIsbn());
            newCopy.setTitle(templateBook.getTitle());
            newCopy.setAuthor(templateBook.getAuthor());
            newCopy.setAvailable(true);
            newCopy.setIssuedToUserId(null);
            books.add(newCopy);
        }
        fileManager.saveBooks(books);
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public boolean deleteAllCopiesByIsbn(String isbn) {
        List<Book> copiesToDelete = new ArrayList<>();
        boolean isbnExists = false;

        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                isbnExists = true;
                if (!book.isAvailable()) {
                    throw new IllegalStateException(
                            "Cannot delete catalog entry because one or more copies are currently checked out.");
                }
                copiesToDelete.add(book);
            }
        }
        if (!isbnExists) {
            return false;
        }

        books.removeAll(copiesToDelete);
        fileManager.saveBooks(books);
        return true;
    }

    public boolean removeSingleCopyById(int bookId) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId() == bookId) {
                books.remove(i);
                fileManager.saveBooks(books);
                return true;
            }
        }
        return false;
    }

    public BookInventoryDto searchCatalogByIsbn(String isbn) {
        BookInventoryDto searchResult = null;
        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                if (searchResult == null) {
                    String extraField = "N/A";
                    if (book instanceof com.nachiket.library_management.model.FictionBook) {
                        extraField = ((com.nachiket.library_management.model.FictionBook) book).getGenre();
                    } else if (book instanceof com.nachiket.library_management.model.AcademicBook) {
                        extraField = ((com.nachiket.library_management.model.AcademicBook) book).getSubject();
                    } else if (book instanceof com.nachiket.library_management.model.Magazine) {
                        extraField = ((com.nachiket.library_management.model.Magazine) book).getIssueNumber();
                    }
                    searchResult = new BookInventoryDto(
                            book.getIsbn(), book.getTitle(), book.getAuthor(), book.getType(), extraField);
                }

                searchResult.incrementTotal();
                if (book.isAvailable()) {
                    searchResult.incrementAvailable();
                }
            }
        }
        return searchResult;
    }

    public boolean updateBookMetadataByIsbn(String isbn, String newTitle, String newAuthor, String extraValue) {
        boolean updatedAny = false;

        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                book.setTitle(newTitle);
                book.setAuthor(newAuthor);
                if (book instanceof FictionBook) {
                    ((FictionBook) book).setGenre(extraValue);
                } else if (book instanceof AcademicBook) {
                    ((AcademicBook) book).setSubject(extraValue);
                } else if (book instanceof Magazine) {
                    ((Magazine) book).setIssueNumber(extraValue);
                }
                updatedAny = true;
            }
        }
        if (updatedAny) {
            fileManager.saveBooks(books);
        }
        return updatedAny;
    }

    public boolean addUser(User user) {
        for (User existingUser : users) {
            if (existingUser.getUserId() == user.getUserId()) {
                System.out.println("Failed to add user: Duplicate ID " + user.getUserId());
                return false;
            }
        }
        users.add(user);
        fileManager.saveUsers(users);
        return true;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public boolean issueBookByIsbn(String isbn, int userId) {
        for (Book book : books) {
            // Find the first book id matching the ISBN that is not borrowed
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn) && book.isAvailable()) {
                book.setAvailable(false);
                book.setIssuedToUserId(userId);
                fileManager.saveBooks(books);
                return true;
            }
        }
        return false;
    }

    public List<Book> getBooksBorrowedByUser(int userId) {
        List<Book> userCopies = new ArrayList<>();
        for (Book book : books) {
            if (!book.isAvailable() && book.getIssuedToUserId() != null && book.getIssuedToUserId() == userId) {
                userCopies.add(book);
            }
        }
        return userCopies;
    }

    public boolean returnBook(int bookId) {
        Book foundBook = null;
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                foundBook = book;
            }
        }
        if (foundBook == null) {
            return false;
        }
        foundBook.setAvailable(true);
        foundBook.setIssuedToUserId(null);
        fileManager.saveBooks(books);
        return true;
    }

    public User searchUser(int userId) {
        for (User user : users) {
            if (user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    public List<BookInventoryDto> getAggregatedInventory() {
        Map<String, BookInventoryDto> inventoryMap = new LinkedHashMap<>();
        for (Book book : books) {
            String isbn = book.getIsbn();
            if (isbn == null || isbn.trim().isEmpty())
                continue;

            if (!inventoryMap.containsKey(isbn)) {
                String extraField = "N/A";
                if (book instanceof FictionBook)
                    extraField = ((FictionBook) book).getGenre();
                else if (book instanceof AcademicBook)
                    extraField = ((AcademicBook) book).getSubject();
                else if (book instanceof Magazine)
                    extraField = ((Magazine) book).getIssueNumber();

                inventoryMap.put(isbn,
                        new BookInventoryDto(isbn, book.getTitle(), book.getAuthor(), book.getType(), extraField));
            }
            BookInventoryDto dto = inventoryMap.get(isbn);
            dto.incrementTotal();
            if (book.isAvailable()) {
                dto.incrementAvailable();
            }
        }
        return new ArrayList<>(inventoryMap.values());

    }

    public boolean registerUser(User newUser) {
        for (User existingUser : users) {
            if (existingUser.getUserId() != null && existingUser.getUserId().equals(newUser.getUserId())) {
                return false;
            }
            if (existingUser.getUniqueIdCard() != null &&
                    existingUser.getUniqueIdCard().equalsIgnoreCase(newUser.getUniqueIdCard())) {
                throw new IllegalArgumentException(
                        "Registration Failed: A user with this Unique Identifier card is already registered.");
            }
        }

        users.add(newUser);
        fileManager.saveUsers(users);
        return true;
    }

    public boolean updateUser(int userId, String newName, String newUniqueIdCard) {
        User target = searchUser(userId);
        if (target == null) {
            return false;
        }
        for (User existing : users) {
            if (existing.getUserId() != userId
                    && existing.getUniqueIdCard() != null
                    && existing.getUniqueIdCard().equalsIgnoreCase(newUniqueIdCard)) {
                throw new IllegalArgumentException("Update Failed: Unique ID card already in use.");
            }
        }
        target.setName(newName);
        target.setUniqueIdCard(newUniqueIdCard);
        fileManager.saveUsers(users);
        return true;
    }

    public boolean deleteUser(int userId) {
        List<Book> borrowed = getBooksBorrowedByUser(userId);
        if (!borrowed.isEmpty()) {
            throw new IllegalStateException("Deletion Blocked: User has " + borrowed.size() + " book(s) checked out.");
        }
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId() == userId) {
                users.remove(i);
                fileManager.saveUsers(users);
                return true;
            }
        }
        return false;
    }
}