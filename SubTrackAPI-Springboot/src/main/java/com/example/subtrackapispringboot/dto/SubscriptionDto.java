package com.example.subtrackapispringboot.dto;

import com.example.subtrackapispringboot.enums.BillingCycle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionDto(
        Integer id,
        String name,
        BigDecimal cost,
        BillingCycle billingCycle,
        LocalDateTime nextRenewalDate,
        String categoryName,
        String paymentMethod,
        String notes
){}

