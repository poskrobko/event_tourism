package com.example.library.service;

import com.example.library.dto.AdminDtos;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibrarianInvitationService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from}")
    private String mailFrom;

    public LibrarianInvitationService(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public AdminDtos.InviteLibrarianResponse invite(AdminDtos.InviteLibrarianRequest request) {
        String email = request.email();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        String normalizedEmail = email.trim();
        User user = userRepository.findByEmail(normalizedEmail).orElseGet(User::new);
        String temporaryPassword = generateTemporaryPassword(14);

        user.setEmail(normalizedEmail);
        String nickname = request.nickname() == null || request.nickname().isBlank()
                ? normalizedEmail.split("@")[0]
                : request.nickname().trim();
        user.setNickname(nickname);
        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(Role.ROLE_LIBRARIAN);
        user.setRoles(roles);
        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        User savedUser = userRepository.save(user);

        sendInviteEmail(savedUser.getEmail(), nickname, temporaryPassword);
        return new AdminDtos.InviteLibrarianResponse(savedUser.getId(), savedUser.getEmail(), temporaryPassword,
                "Invitation sent");
    }

    private void sendInviteEmail(String email, String nickname, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Library access for librarian account");
        message.setText("Hello " + nickname + ",\n\n"
                + "Your librarian account has been created.\n"
                + "Email: " + email + "\n"
                + "Temporary password: " + temporaryPassword + "\n\n"
                + "Please sign in and change your password as soon as possible.");
        mailSender.send(message);
    }

    private String generateTemporaryPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
