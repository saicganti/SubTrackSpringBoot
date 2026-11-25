package com.example.subtrackapispringboot.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(
        BigDecimal totalMonthlySpend,
        BigDecimal totalYearlySpend,
        List<SubscriptionDto> upcomingRenewals7Days,
        List<SubscriptionDto> upcomingRenewals30Days,
        List<CategorySpendDto> spendingByCategory
) {}
