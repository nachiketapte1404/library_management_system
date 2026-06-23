package com.nachiket.library_management.controller;

import com.nachiket.library_management.model.AcademicBook;
import com.nachiket.library_management.model.Book;
import com.nachiket.library_management.model.BookInventoryDto;
import com.nachiket.library_management.model.FictionBook;
import com.nachiket.library_management.model.Magazine;
import com.nachiket.library_management.service.LibraryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/books")
@CrossOrigin("*")
public class BookController {

    private final LibraryService libraryService;

    BookController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/check/{isbn}")
    public ResponseEntity<Book> checkIsbn(@PathVariable String isbn) {
        Book existingBook = libraryService.findBookByIsbn(isbn);
        if (existingBook == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(existingBook);
    }

    @GetMapping
    public List<BookInventoryDto> getBook() {
        return libraryService.getAggregatedInventory();
    }

    @GetMapping("/search/{isbn}")
    public ResponseEntity<BookInventoryDto> searchBookByIsbn(@PathVariable String isbn) {
        BookInventoryDto result = libraryService.searchCatalogByIsbn(isbn);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public List<Book> getBorrowedBooks(@PathVariable int userId) {
        return libraryService.getBooksBorrowedByUser(userId);
    }

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Map<String, Object> payload) {
        try {
            String isbn = (String) payload.get("isbn");
            if (isbn == null || isbn.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ISBN number not found in payload");
            }

            int quantity;
            try {
                quantity = Integer.parseInt(payload.get("quantity").toString());
            } catch (NumberFormatException | NullPointerException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity must be a valid number.");
            }
            if (quantity <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Quantity must be greater than 0.");
            }

            // Check if the book already exists in our system
            Book existingBook = libraryService.findBookByIsbn(isbn);

            if (existingBook != null) {
                // BACKEND METADATA MISMATCH GUARD:
                String incomingTitle = (String) payload.get("title");
                String incomingAuthor = (String) payload.get("author");

                // If they provided a title/author in the payload, make sure it matches what's in our DB
                if (incomingTitle != null && !incomingTitle.trim().isEmpty() && !existingBook.getTitle().equalsIgnoreCase(incomingTitle.trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Inventory Conflict: The provided title does not match the registered book catalog for ISBN " + isbn);
                }
                if (incomingAuthor != null && !incomingAuthor.trim().isEmpty() && !existingBook.getAuthor().equalsIgnoreCase(incomingAuthor.trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Inventory Conflict: The provided author does not match the registered book catalog for ISBN " + isbn);
                }

                // RESTOCK FLOW: Everything is safe, update quantity
                libraryService.addBooksBatch(existingBook, quantity);
                return ResponseEntity.ok(quantity + " extra copies of '" + existingBook.getTitle() + "' restocked successfully.");
            }

            // NEW BOOK FLOW: The book doesn't exist, validate metadata fields
            String title = (String) payload.get("title");
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Book title not found in payload");
            }
            String author = (String) payload.get("author");
            if (author == null || author.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Author not found in payload");
            }

            String type = payload.getOrDefault("type", "GENERAL").toString();
            Book templateBook;
            if ("FICTION".equalsIgnoreCase(type)) {
                String genre = payload.getOrDefault("genre", "General").toString();
                templateBook = new FictionBook(0, isbn, title, author, genre);
            } else if ("ACADEMIC".equalsIgnoreCase(type)) {
                String subject = payload.getOrDefault("subject", "General").toString();
                templateBook = new AcademicBook(0, isbn, title, author, subject);
            } else if ("MAGAZINE".equalsIgnoreCase(type)) {
                String issueNumber = payload.getOrDefault("issueNumber", "N/A").toString();
                templateBook = new Magazine(0, isbn, title, author, issueNumber);
            } else {
                templateBook = new Book(0, isbn, title, author);
            }

            libraryService.addBooksBatch(templateBook, quantity);
            return ResponseEntity.ok(quantity + " copies added successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing inventory request: " + e.getMessage());
        }
    }

    @PostMapping("/issue/{isbn}/{userId}")
    public ResponseEntity<String> issueBook(@PathVariable String isbn, @PathVariable int userId) {
        boolean issued = libraryService.issueBookByIsbn(isbn, userId);
        if (issued) {
            return ResponseEntity.ok("Book checked out successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Issue Failed: No available copies left for ISBN: " + isbn);

        }
    }

    @PostMapping("/{bookId}/return")
    public ResponseEntity<String> returnBook(@PathVariable int bookId) {
        boolean returned = libraryService.returnBook(bookId);

        if (returned) {
            return ResponseEntity.ok("Book Returned Successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Return Failed: Book ID not found.");
        }
    }

    @PutMapping("/update/{isbn}")
    public ResponseEntity<String> updateBookMetadata(@PathVariable String isbn,
            @RequestBody Map<String, String> payload) {
        String title = payload.get("title");
        String author = payload.get("author");

        if (title == null || author == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required update values.");
        }

        String extraValue = payload.getOrDefault("extraValue",
                payload.getOrDefault("genre",
                        payload.getOrDefault("subject",
                                payload.getOrDefault("issueNumber", "N/A"))));

        boolean success = libraryService.updateBookMetadataByIsbn(isbn, title, author, extraValue);

        if (success) {
            return ResponseEntity.ok("Catalog metadata updated and cascaded to all physical copies successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Update Failed: Target ISBN not found in network registry.");
        }
    }

    @DeleteMapping("/catalog/{isbn}")
    public ResponseEntity<String> deleteCatalogTitle(@PathVariable String isbn) {
        try {
            boolean deleted = libraryService.deleteAllCopiesByIsbn(isbn);
            if (deleted) {
                return ResponseEntity.ok("Catalog title and all copies deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delete Failed: ISBN not found.");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/lost/{bookId}")
    public ResponseEntity<String> reportLostBook(@PathVariable int bookId) {
        boolean removed = libraryService.removeSingleCopyById(bookId);
        if (removed) {
            return ResponseEntity.ok("Book copy ID " + bookId + " marked as lost and removed from inventory tracking.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lost Log Error: Physical Book ID " + bookId + " does not exist.");
        }
    }
}