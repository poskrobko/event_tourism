package com.example.eventtourism.controller;

import com.example.eventtourism.dto.AuthDtos;
import com.example.eventtourism.dto.CommonDtos;
import com.example.eventtourism.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonDtos.ApiMessage> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new CommonDtos.ApiMessage("User registered"));
    }

    @PostMapping("/login")
    public AuthDtos.AuthResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return authService.login(request);
    }
}
