package com.example.subtrackapispringboot;

import com.example.subtrackapispringboot.dto.AuthResponseDto;
import com.example.subtrackapispringboot.dto.LoginDto;
import com.example.subtrackapispringboot.dto.RegisterDto;
import com.example.subtrackapispringboot.entity.User;
import com.example.subtrackapispringboot.exception.ConflictException;
import com.example.subtrackapispringboot.exception.UnauthorizedException;
import com.example.subtrackapispringboot.repository.UserRepository;
import com.example.subtrackapispringboot.service.AuthService;
import com.example.subtrackapispringboot.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1);
        existingUser.setEmail("test@example.com");
        existingUser.setPasswordHash("$2a$10$hashedpassword");
        existingUser.setFirstName("Test");
        existingUser.setLastName("User");
    }

    // ---------- register ----------

    @Test
    void register_newEmail_returnsTokenAndSavesUser() {
        RegisterDto dto = new RegisterDto(
                "new@example.com", "TestPass123", "New", "User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("TestPass123")).thenReturn("$2a$10$newHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2);
            return u;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt-token");

        AuthResponseDto result = authService.register(dto);

        assertNotNull(result);
        assertEquals("fake-jwt-token", result.token());
        assertEquals("new@example.com", result.email());
        assertEquals("New", result.firstName());
        assertEquals("User", result.lastName());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("$2a$10$newHash", saved.getPasswordHash());
        assertEquals("New", saved.getFirstName());
        assertEquals("User", saved.getLastName());

        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_existingEmail_throwsConflictAndDoesNotSave() {
        RegisterDto dto = new RegisterDto(
                "test@example.com", "TestPass123", "Test", "User");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> authService.register(dto));

        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(jwtService, passwordEncoder);
    }

    // ---------- login ----------

    @Test
    void login_validCredentials_returnsToken() {
        LoginDto dto = new LoginDto("test@example.com", "TestPass123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(eq("TestPass123"), eq("$2a$10$hashedpassword")))
                .thenReturn(true);
        when(jwtService.generateToken(existingUser)).thenReturn("fake-jwt-token");

        AuthResponseDto result = authService.login(dto);

        assertNotNull(result);
        assertEquals("fake-jwt-token", result.token());
        assertEquals("test@example.com", result.email());
        assertEquals("Test", result.firstName());
        assertEquals("User", result.lastName());

        verify(passwordEncoder).matches("TestPass123", "$2a$10$hashedpassword");
        verify(jwtService).generateToken(existingUser);
    }

    @Test
    void login_wrongPassword_throwsUnauthorizedAndDoesNotGenerateToken() {
        LoginDto dto = new LoginDto("test@example.com", "WrongPassword");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(dto));

        assertEquals("Invalid email or password", ex.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_unknownEmail_throwsUnauthorizedAndDoesNotCheckPassword() {
        LoginDto dto = new LoginDto("nobody@example.com", "TestPass123");

        when(userRepository.findByEmail("nobody@example.com"))
                .thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(dto));

        assertEquals("Invalid email or password", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verifyNoInteractions(jwtService);
    }
}
