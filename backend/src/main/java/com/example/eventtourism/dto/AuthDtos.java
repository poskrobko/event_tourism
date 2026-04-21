package com.example.eventtourism.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record RegisterRequest(@Email String email, @NotBlank String password, @NotBlank String fullName) {}
    public record LoginRequest(@Email String email, @NotBlank String password) {}
    public record AuthResponse(String token, String role) {}
}
