package com.example.subtrackapispringboot.service;

import com.example.subtrackapispringboot.dto.AuthResponseDto;
import com.example.subtrackapispringboot.dto.LoginDto;
import com.example.subtrackapispringboot.dto.RegisterDto;
import com.example.subtrackapispringboot.entity.User;
import com.example.subtrackapispringboot.exception.ConflictException;
import com.example.subtrackapispringboot.exception.UnauthorizedException;
import com.example.subtrackapispringboot.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDto register(RegisterDto dto){
        if(userRepository.existsByEmail(dto.email())){
            throw new ConflictException("Email already in use");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return toResponse(user);
    }

    private AuthResponseDto toResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponseDto(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
