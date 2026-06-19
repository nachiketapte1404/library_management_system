package com.nachiket.library_management.controller;

import com.nachiket.library_management.model.User;
import com.nachiket.library_management.service.LibraryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin("*")
public class UserController {

    private final LibraryService libraryService;

    public UserController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody User user) {
        if (user.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration Failed: User ID is required.");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration Failed: User name cannot be empty.");
        }
        if (user.getUniqueIdCard() == null || user.getUniqueIdCard().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Registration Failed: Unique Identification Card number is required.");
        }
        try {
            boolean success = libraryService.registerUser(user);
            if (!success) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Error: A user with this ID already exists.");
            }
            return ResponseEntity.ok("User registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping
    public List<User> getUsers() {
        return libraryService.getAllUsers();
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<String> updateUser(@PathVariable int userId, @RequestBody User userUpdate) {
        if (userUpdate.getName() == null || userUpdate.getUniqueIdCard() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required update values.");
        }
        try {
            boolean updated = libraryService.updateUser(userId, userUpdate.getName(), userUpdate.getUniqueIdCard());
            if (updated) {
                return ResponseEntity.ok("User details updated successfully.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Update Failed: User ID " + userId + " not found.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId) {
        try {
            boolean deleted = libraryService.deleteUser(userId);
            if (deleted) {
                return ResponseEntity.ok("User deleted successfully.");
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Delete Failed: User ID not found.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}