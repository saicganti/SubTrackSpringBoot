package com.example.subtrackapispringboot.dto;

public record AuthResponseDto(
        String token,
        String email,
        String firstName,
        String lastName
) {}
