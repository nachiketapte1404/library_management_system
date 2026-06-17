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
        boolean success = libraryService.addUser(user);
        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: A user with this ID already exists");
        }
        return ResponseEntity.ok("User added successfully!");
    }

    @GetMapping
    public List<User> getUsers() {

        return libraryService.getAllUsers();
    }
}
