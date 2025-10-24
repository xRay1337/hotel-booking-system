package org.service.booking.controller;

import lombok.RequiredArgsConstructor;
import org.service.booking.dto.AuthRequest;
import org.service.booking.dto.AuthResponse;
import org.service.booking.dto.UserDTO;
import org.service.booking.entity.User;
import org.service.booking.service.UserService;
import org.service.booking.mapper.UserMapper;
import org.service.booking.util.CorrelationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        CorrelationContext.initCorrelationIdIfAbsent();
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        CorrelationContext.initCorrelationIdIfAbsent();
        AuthResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        CorrelationContext.initCorrelationIdIfAbsent();
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        CorrelationContext.initCorrelationIdIfAbsent();
        User user = userService.getUserById(id);
        UserDTO userDTO = userMapper.toDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody AuthRequest request) {
        CorrelationContext.initCorrelationIdIfAbsent();
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        CorrelationContext.initCorrelationIdIfAbsent();
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}