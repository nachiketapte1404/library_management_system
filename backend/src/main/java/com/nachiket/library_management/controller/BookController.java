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
            return ResponseEntity.noContent().build(); // Return 204 if brand new ISBN
        }
        return ResponseEntity.ok(existingBook); // Return 200 with metadata if it exists
    }

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Map<String, Object> payload) {
        try {
            String isbn = (String) payload.get("isbn");
            String title = (String) payload.get("title");
            String author = (String) payload.get("author");
            int quantity = Integer.parseInt(payload.get("quantity").toString());
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

    @GetMapping
    public List<BookInventoryDto> getBook() {
        return libraryService.getAggregatedInventory();
    }
    // public List<Book> getBooks() {

    //     return libraryService.getAllBooks();
    // }

    @GetMapping("/{bookId}")
    public Book searchBook(@PathVariable int bookId) {

        return libraryService.searchBook(bookId);
    }

    @DeleteMapping("/{bookId}")
    public String deleteBook(@PathVariable int bookId) {
        boolean deleted = libraryService.deleteBook(bookId);
        if (deleted) {
            return "Book Deleted";
        }
        return "Book Not Found";
    }

    // @PostMapping("/{bookId}/issue/{userId}")
    @PostMapping("/issue/{isbn}/{userId}")
    public ResponseEntity<String> issueBook(@PathVariable String isbn, @PathVariable int userId) {
        boolean issued = libraryService.issueBookByIsbn(isbn, userId);
        if(issued)
        {
            System.out.println("Issued to user: " + userId);
            return ResponseEntity.ok("Book checked out successfully");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Issue Failed: No available copies left for ISBN: " + isbn);

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
}