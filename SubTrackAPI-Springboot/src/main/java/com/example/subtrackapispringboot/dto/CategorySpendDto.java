package com.example.subtrackapispringboot.dto;

import com.example.subtrackapispringboot.entity.Category;

import java.math.BigDecimal;

public record CategorySpendDto(
        int categoryId,
        String categoryName,
        BigDecimal amount
) {}
