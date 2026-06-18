package com.nachiket.library_management.service;

import com.nachiket.library_management.model.AcademicBook;
import com.nachiket.library_management.model.Book;
import com.nachiket.library_management.model.BookInventoryDto;
import com.nachiket.library_management.model.FictionBook;
import com.nachiket.library_management.model.Magazine;
import com.nachiket.library_management.model.User;
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

    public Book searchBook(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                return book;
            }
        }
        return null;
    }

    // public boolean deleteBook(int bookId) {

    // boolean deleted = books.removeIf(book -> book.getBookId() == bookId);
    // if (deleted) {
    // fileManager.saveBooks(books);
    // }

    // return deleted;
    // }

    public String deleteAllCopiesByIsbn(String isbn) {
        List<Book> copiesToDelete = new ArrayList<>();

        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                // Rule check: If any copy is currently checked out, block the whole transaction
                if (!book.isAvailable()) {
                    return "Deletion Blocked: Cannot delete catalog entry because one or more copies are currently checked out.";
                }
                copiesToDelete.add(book);
            }
        }

        if (copiesToDelete.isEmpty()) {
            return "Error: No books found matching ISBN " + isbn;
        }

        // Perform bulk removal
        books.removeAll(copiesToDelete);
        fileManager.saveBooks(books);
        return "SUCCESS: All physical copies under ISBN " + isbn + " have been removed.";
    }

    public boolean removeSingleCopyById(int bookId) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId() == bookId) {
                books.remove(i);
                fileManager.saveBooks(books); // Commit change to books.txt
                return true;
            }
        }
        return false;
    }

    // Inside LibraryService.java

    public BookInventoryDto searchCatalogByIsbn(String isbn) {
        BookInventoryDto searchResult = null;

        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                // Initialize the DTO matching this title's metadata if we haven't yet
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

                // Dynamically increment catalog counters
                searchResult.incrementTotal();
                if (book.isAvailable()) {
                    searchResult.incrementAvailable();
                }
            }
        }

        return searchResult; // Returns null if no copies found with this ISBN
    }

    public boolean updateBookMetadataByIsbn(String isbn, String newTitle, String newAuthor, String extraValue) {
        boolean updatedAny = false;

        for (Book book : books) {
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn)) {
                book.setTitle(newTitle);
                book.setAuthor(newAuthor);

                if (book instanceof com.nachiket.library_management.model.FictionBook) {
                    ((com.nachiket.library_management.model.FictionBook) book).setGenre(extraValue);
                } else if (book instanceof com.nachiket.library_management.model.AcademicBook) {
                    ((com.nachiket.library_management.model.AcademicBook) book).setSubject(extraValue);
                } else if (book instanceof com.nachiket.library_management.model.Magazine) {
                    ((com.nachiket.library_management.model.Magazine) book).setIssueNumber(extraValue);
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

    public boolean issueBook(int bookId, int userId) {
        Book book = searchBook(bookId);
        User user = searchUser(userId);
        if (book == null) {
            System.out.println("Issue Failed: Book with ID " + bookId + " not found.");
            return false;
        }
        System.out.println("Before issue - Assigned User ID: " + book.getIssuedToUserId());
        if (user == null) {
            System.out.println("Issue Failed: User with ID " + userId + " not found.");
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("Issue Failed: Book is already issued.");
            return false;
        }
        book.setAvailable(false);
        book.setIssuedToUserId(userId);
        System.out.println("After issue - Assigned User ID: " + book.getIssuedToUserId());

        fileManager.saveBooks(books);

        return true;
    }

    public boolean issueBookByIsbn(String isbn, int userId) {
        for (Book book : books) {
            // Find the first physical copy matching the ISBN that is currently free
            if (book.getIsbn() != null && book.getIsbn().equalsIgnoreCase(isbn) && book.isAvailable()) {
                book.setAvailable(false);
                book.setIssuedToUserId(userId);
                fileManager.saveBooks(books);
                System.out.println("Auto-assigned Physical Book ID: " + book.getBookId() + " to user: " + userId);
                return true;
            }
        }
        return false; // No copies left or ISBN doesn't exist
    }

    public List<Book> getBooksBorrowedByUser(int userId) {
        List<Book> userCopies = new ArrayList<>();
        for (Book book : books) {
            // If the book is checked out and matches the requested User ID
            if (!book.isAvailable() && book.getIssuedToUserId() != null && book.getIssuedToUserId() == userId) {
                userCopies.add(book);
            }
        }
        return userCopies;
    }

    public boolean returnBook(int bookId) {

        Book book = searchBook(bookId);

        if (book == null) {
            return false;
        }

        book.setAvailable(true);
        book.setIssuedToUserId(null);

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

    public String registerUser(User newUser) {
        for (User existingUser : users) {
            // 1. Check if the system-generated numeric User ID already exists
            if (existingUser.getUserId() == newUser.getUserId()) {
                return "Registration Failed: User ID " + newUser.getUserId() + " is already taken.";
            }

            // 2. Check if the text-based Unique ID Card string already exists
            if (existingUser.getUniqueIdCard() != null &&
                    existingUser.getUniqueIdCard().equalsIgnoreCase(newUser.getUniqueIdCard())) {
                return "Registration Failed: A user with this Unique Identifier card is already registered.";
            }
        }
        users.add(newUser);
        fileManager.saveUsers(users); // Make sure your fileManager handles the 3rd column now!
        return "SUCCESS: User registered successfully.";
    }
}