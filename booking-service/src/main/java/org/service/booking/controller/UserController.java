package org.service.booking.controller;

import lombok.RequiredArgsConstructor;
import org.service.booking.dto.UserDTO;
import org.service.booking.dto.UserLoginRequest;
import org.service.booking.dto.UserRegistrationRequest;
import org.service.booking.entity.User;
import org.service.booking.mapper.UserMapper;
import org.service.booking.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserRegistrationRequest request) {
        User user = userMapper.toEntity(request);
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(userMapper.toDTO(createdUser));
    }

    @PostMapping("/auth")
    public ResponseEntity<UserDTO> authenticateUser(@RequestBody UserLoginRequest request) {
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("id") Long id, @RequestBody UserRegistrationRequest request) {
        User user = userMapper.toEntity(request);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}