package com.example.subtrackapispringboot.dto;

import com.example.subtrackapispringboot.entity.Category;
import com.example.subtrackapispringboot.enums.BillingCycle;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateSubscriptionDto(
     @NotNull @Size(max=50) String name,
     @DecimalMin(value="0.01") @DecimalMax(value="10000") BigDecimal cost,
     BillingCycle billingCycle,
     @NotNull LocalDateTime nextRenewalDate,
     Integer categoryId,
     @Size(max=50) String paymentMethod,
     @Size(max=500) String notes
) {}
