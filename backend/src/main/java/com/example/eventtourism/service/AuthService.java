package com.example.eventtourism.service;

import com.example.eventtourism.dto.AuthDtos;
import com.example.eventtourism.entity.RoleType;
import com.example.eventtourism.entity.User;
import com.example.eventtourism.exception.BadRequestException;
import com.example.eventtourism.repository.RoleRepository;
import com.example.eventtourism.repository.UserRepository;
import com.example.eventtourism.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(AuthDtos.RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRoles(Set.of(roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new BadRequestException("Role USER not found"))));
        userRepository.save(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Wrong credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        String role = user.getRoles().stream().findFirst().map(r -> r.getName().name()).orElse("USER");
        return new AuthDtos.AuthResponse(jwtService.generateToken(user.getEmail(), role), role);
    }
}
