package com.example.demo.controller;

import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @GetMapping
public ResponseEntity<List<UserResponse>> getAll() {
    return ResponseEntity.ok(
        userService.getAllUsers().stream()
            .map(UserResponse::from)
            .toList()
    );
}

@PostMapping
public ResponseEntity<UserResponse> create(@RequestBody User user) {
    return ResponseEntity.ok(UserResponse.from(userService.createUser(user)));
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}