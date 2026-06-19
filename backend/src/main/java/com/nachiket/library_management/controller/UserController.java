package com.nachiket.library_management.controller;

import com.nachiket.library_management.model.User;
import com.nachiket.library_management.service.LibraryService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/update/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable int userId,
            @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String uniqueIdCard = payload.get("uniqueIdCard");
        if (name == null || uniqueIdCard == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing required update values.");
        }
        String result = libraryService.updateUser(userId, name, uniqueIdCard);
        if (result.startsWith("SUCCESS")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId) {
        String result = libraryService.deleteUser(userId);
        if (result.startsWith("SUCCESS")) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
