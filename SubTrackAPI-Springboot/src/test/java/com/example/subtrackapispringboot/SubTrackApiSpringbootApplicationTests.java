package com.example.subtrackapispringboot;

import com.example.subtrackapispringboot.dto.CreateSubscriptionDto;
import com.example.subtrackapispringboot.dto.DashboardDto;
import com.example.subtrackapispringboot.dto.SubscriptionDto;
import com.example.subtrackapispringboot.entity.Category;
import com.example.subtrackapispringboot.entity.Subscription;
import com.example.subtrackapispringboot.enums.BillingCycle;
import com.example.subtrackapispringboot.repository.CategoryRepository;
import com.example.subtrackapispringboot.repository.SubscriptionRepository;
import com.example.subtrackapispringboot.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubTrackApiSpringbootApplicationTests {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;
    private static Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Test Category");
    }

    private static CreateSubscriptionDto makeDto(BigDecimal cost, BillingCycle billingCycle, int daysUntilRenewal, Category category){
        return new CreateSubscriptionDto(
         "Test",
                cost,
                billingCycle,
                LocalDateTime.now().plusDays(daysUntilRenewal),
                testCategory.getId(),
                "Visa",
                ""
        );
    }

    @Test
    public void create_returnsDtoWithCategoryName(){

        lenient().when(categoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            Subscription sub = invocation.getArgument(0);
            sub.setId(100);
            return sub;
        });

        CreateSubscriptionDto dto =  makeDto(new BigDecimal("15.99"), BillingCycle.MONTHLY, 10, testCategory);
        SubscriptionDto result = subscriptionService.create(dto, "user1");

        assertNotNull(result);
        assertEquals("Test Category", result.categoryName());
        assertEquals(0, new BigDecimal("15.99").compareTo(result.cost()));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void dashboard_computesMonthlySpendFromMixedCycles() {
        Subscription weekly = buildSubscription(1, "user", new BigDecimal("10"), BillingCycle.WEEKLY, 3);
        Subscription monthly = buildSubscription(2, "user", new BigDecimal("20"), BillingCycle.MONTHLY, 15);
        Subscription yearly = buildSubscription(3, "user", new BigDecimal("120"), BillingCycle.YEARLY, 200);

        when(subscriptionRepository.findByUserIdOrderByNextRenewalDate("user"))
                .thenReturn(List.of(weekly, monthly, yearly));

        DashboardDto dashResult = subscriptionService.getDashboard("user");

        BigDecimal expectedMonthly = new BigDecimal("10")
                .multiply(new BigDecimal("4.33"))
                .add(new BigDecimal("20"))
                .add(new BigDecimal("120").divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP));

        assertNotNull(dashResult);
        assertEquals(0, expectedMonthly.compareTo(dashResult.totalMonthlySpend()));
    }

    @Test
    void delete_removesSubscription() {
        List<Subscription> stored = new ArrayList<>();
        Subscription existing = buildSubscription(1, "user1", new BigDecimal("5"), BillingCycle.MONTHLY, 10);
        stored.add(existing);

        when(subscriptionRepository.findByIdAndUserId(1, "user1")).thenReturn(existing);
        doAnswer(invocation -> {
            stored.remove(existing);
            return null;
        }).when(subscriptionRepository).delete(existing);
        when(subscriptionRepository.findByUserIdOrderByNextRenewalDate("user1")).thenReturn(stored);

        assertTrue(subscriptionService.delete(1, "user1"));
        assertTrue(subscriptionService.getAll("user1").isEmpty());
    }

    private Subscription buildSubscription(
            int id, String userId, BigDecimal cost, BillingCycle cycle, int daysUntilRenewal) {
        Subscription sub = new Subscription();
        sub.setId(id);
        sub.setUserId(userId);
        sub.setName("Test");
        sub.setCost(cost);
        sub.setBillingCycle(cycle);
        sub.setNextRenewalDate(LocalDateTime.now().plusDays(daysUntilRenewal));
        sub.setCategory(testCategory);
        return sub;
    }
}

