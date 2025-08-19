package com.rentora.api.controller;

import com.rentora.api.entity.User;
import com.rentora.api.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Test find by email
    @GetMapping("/find")
    public Optional<User> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email);
    }

    // Test save new user
    @PostMapping("/save")
    public User saveUser(@RequestBody User user) {
        return userService.saveUser(user);
    }
}
