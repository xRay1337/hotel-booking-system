package org.service.booking.controller;

import lombok.RequiredArgsConstructor;
import org.service.booking.dto.AuthRequest;
import org.service.booking.dto.AuthResponse;
import org.service.booking.dto.UserDTO;
import org.service.booking.entity.User;
import org.service.booking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")  // Обратите внимание - /user, а не /api/v1/users
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Публичные эндпоинты (без аутентификации)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    // Защищенные эндпоинты (только для ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        UserDTO userDTO = userMapper.toDTO(user);  // ← Преобразуйте entity в DTO
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody AuthRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}