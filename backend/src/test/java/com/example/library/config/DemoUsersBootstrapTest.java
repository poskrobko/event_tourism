package com.example.library.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DemoUsersBootstrapTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DemoUsersBootstrap demoUsersBootstrap;

    @Test
    void run_assignsExpectedRolesToNewDemoUsers() {
        ReflectionTestUtils.setField(demoUsersBootstrap, "seedEnabled", true);
        ReflectionTestUtils.setField(demoUsersBootstrap, "demoPassword", "demo-pass");

        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(passwordEncoder.encode("demo-pass")).thenReturn("encoded-pass");

        demoUsersBootstrap.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(4)).save(userCaptor.capture());

        assertThat(userCaptor.getAllValues())
                .extracting(User::getEmail, User::getRoles)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("anna.reader@library.local", Set.of(Role.ROLE_USER)),
                        org.assertj.core.groups.Tuple.tuple("boris.reader@library.local", Set.of(Role.ROLE_USER)),
                        org.assertj.core.groups.Tuple.tuple("librarian@library.local", Set.of(Role.ROLE_USER, Role.ROLE_LIBRARIAN)),
                        org.assertj.core.groups.Tuple.tuple("admin@library.local", Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
                );
    }

    @Test
    void run_preservesExistingRolesWhileAddingRequiredOnes() {
        ReflectionTestUtils.setField(demoUsersBootstrap, "seedEnabled", true);
        ReflectionTestUtils.setField(demoUsersBootstrap, "demoPassword", "demo-pass");

        User existingAdmin = new User();
        existingAdmin.setEmail("admin@library.local");
        existingAdmin.setNickname("admin");
        existingAdmin.setRoles(new HashSet<>(Set.of(Role.ROLE_LIBRARIAN)));

        when(userRepository.findByEmail("anna.reader@library.local")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("boris.reader@library.local")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("librarian@library.local")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@library.local")).thenReturn(Optional.of(existingAdmin));
        when(passwordEncoder.encode("demo-pass")).thenReturn("encoded-pass");

        demoUsersBootstrap.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(4)).save(userCaptor.capture());

        User savedAdmin = userCaptor.getAllValues().stream()
                .filter(user -> "admin@library.local".equals(user.getEmail()))
                .findFirst()
                .orElseThrow();

        assertThat(savedAdmin.getRoles()).containsExactlyInAnyOrder(Role.ROLE_USER, Role.ROLE_ADMIN, Role.ROLE_LIBRARIAN);
    }
}
