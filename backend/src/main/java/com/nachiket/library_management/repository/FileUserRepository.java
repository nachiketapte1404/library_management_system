package com.nachiket.library_management.repository;

import com.nachiket.library_management.model.User;
import org.springframework.stereotype.Repository;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FileUserRepository {
    private static final String users_records_file_name = "users.txt";
    public void saveUsers(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(users_records_file_name))) {
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

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(users_records_file_name);
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
                    String uniqueIdCard = (data.length > 2) ? data[2] : "N/A";
                    users.add(new User(id, name, uniqueIdCard));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading users registry tracking records: " + e.getMessage());
        }
        return users;
    }
}