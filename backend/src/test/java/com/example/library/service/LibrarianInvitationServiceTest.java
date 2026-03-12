package com.example.library.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.library.dto.AdminDtos;
import com.example.library.model.Role;
import com.example.library.model.User;
import com.example.library.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LibrarianInvitationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private LibrarianInvitationService librarianInvitationService;

    @Test
    void invite_preservesExistingRolesAndAddsLibrarianRole() {
        ReflectionTestUtils.setField(librarianInvitationService, "mailFrom", "noreply@library.local");

        User existingUser = new User();
        existingUser.setEmail("admin@library.local");
        existingUser.setNickname("admin");
        existingUser.setRoles(new HashSet<>(Set.of(Role.ROLE_ADMIN)));

        when(userRepository.findByEmail("admin@library.local")).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-temp-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminDtos.InviteLibrarianRequest request = new AdminDtos.InviteLibrarianRequest("admin@library.local", "Admin");
        librarianInvitationService.invite(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        Set<Role> savedRoles = userCaptor.getValue().getRoles();
        assertThat(savedRoles).contains(Role.ROLE_ADMIN, Role.ROLE_LIBRARIAN);

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void invite_throwsWhenEmailIsNull() {
        AdminDtos.InviteLibrarianRequest request = new AdminDtos.InviteLibrarianRequest(null, "Admin");

        assertThatThrownBy(() -> librarianInvitationService.invite(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }
}
