package com.example.subtrackapispringboot.service;

import com.example.subtrackapispringboot.dto.CategorySpendDto;
import com.example.subtrackapispringboot.dto.CreateSubscriptionDto;
import com.example.subtrackapispringboot.dto.DashboardDto;
import com.example.subtrackapispringboot.dto.SubscriptionDto;
import com.example.subtrackapispringboot.entity.Category;
import com.example.subtrackapispringboot.entity.Subscription;
import com.example.subtrackapispringboot.exception.ResourceNotFoundException;
import com.example.subtrackapispringboot.repository.CategoryRepository;
import com.example.subtrackapispringboot.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final CategoryRepository categoryRepository;
    @ReadOnlyProperty
    private SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, CategoryRepository categoryRepository){
        this.subscriptionRepository = subscriptionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public List<SubscriptionDto> getAll(String userId){
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByNextRenewalDate(userId);

        return subscriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDto getById(int id, String userId){
        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId);

        if(subscription == null){
            return null;
        }
        else{
            return mapToDto(subscription);
        }
    }

    @Transactional
    public SubscriptionDto create(CreateSubscriptionDto dto, String userId){

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Subscription subscription = new Subscription();
        subscription.setName(dto.name());
        subscription.setCost(dto.cost());
        subscription.setBillingCycle(dto.billingCycle());
        subscription.setNextRenewalDate(dto.nextRenewalDate());
        subscription.setCategory(category);
        subscription.setPaymentMethod(dto.paymentMethod());
        subscription.setNotes(dto.notes());
        subscription.setUserId(userId);

        subscriptionRepository.save(subscription);

        return mapToDto(subscription);
    }

    @Transactional
    public SubscriptionDto update(Integer id, CreateSubscriptionDto dto, String userId){

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId);

        if(subscription == null){
            return null;
        }

        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        subscription.setName(dto.name());
        subscription.setCost(dto.cost());
        subscription.setBillingCycle(dto.billingCycle());
        subscription.setNextRenewalDate(dto.nextRenewalDate());
        subscription.setCategory(category);
        subscription.setPaymentMethod(dto.paymentMethod());
        subscription.setNotes(dto.notes());
        subscription.setUserId(userId);

        subscriptionRepository.save(subscription);
        return mapToDto(subscription);
    }

    @Transactional
    public boolean delete(@PathVariable("id") Integer id, String userId){

        Subscription subscription = subscriptionRepository.findByIdAndUserId(id, userId);

        if(subscription == null){
            return false;
        }

        subscriptionRepository.delete(subscription);
        return true;
    }

    @Transactional
    public DashboardDto getDashboard(String userId) {

        List<Subscription> subscriptions = subscriptionRepository.findByUserIdOrderByNextRenewalDate(userId);

        BigDecimal totalMonthlySpend = BigDecimal.ZERO;
        for (Subscription s : subscriptions){
            totalMonthlySpend = totalMonthlySpend.add(getMonthlyCost(s));
        }

        BigDecimal totalYearlySpend = totalMonthlySpend.multiply(BigDecimal.valueOf(12));

        List<SubscriptionDto> next7Days = getRenewals(userId, 7);

        List<SubscriptionDto> next30Days = getRenewals(userId, 30);

        List<CategorySpendDto> byCategory = getSpendByCategory(subscriptions);

        return new DashboardDto(
                totalMonthlySpend,
                totalYearlySpend,
                next7Days,
                next30Days,
                byCategory
        );
    }

    private BigDecimal getMonthlyCost(Subscription s){

        return switch (s.getBillingCycle()) {
            case WEEKLY -> s.getCost().multiply(BigDecimal.valueOf(4.33)) ;
            case YEARLY -> s.getCost().divide(BigDecimal.valueOf(12),2, RoundingMode.HALF_UP);
            default -> s.getCost();
        };
    }

    public List<SubscriptionDto> getRenewals(String userId, Integer days){
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime daysFromNow = today.plusDays(days);

        List<Subscription> subscriptions = subscriptionRepository.findByUserIdAndNextRenewalDateBetweenOrderByNextRenewalDate(userId, today, daysFromNow);

        return subscriptions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CategorySpendDto> getSpendByCategory(List<Subscription> subscriptions) {

        // 1. Define a helper record for the composite grouping key
        record CategoryKey(Integer categoryId, String categoryName) {}

        // 2. Group subscriptions and sum their monthly costs using BigDecimal reduction
        Map<CategoryKey, BigDecimal> groupedMap = subscriptions.stream()
                .collect(Collectors.groupingBy(
                        s -> new CategoryKey(
                                s.getCategory() != null ? s.getCategory().getId() : null,
                                (s.getCategory() != null && s.getCategory().getName() != null)
                                        ? s.getCategory().getName()
                                        : "Uncategorized"
                        ),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                this::getMonthlyCost,
                                BigDecimal::add
                        )
                ));

        // 3. Map the grouped map entries to CategorySpendDto and sort descending by amount
        return groupedMap.entrySet().stream()
                .map(entry -> {
                    return new CategorySpendDto(entry.getKey().categoryId(), entry.getKey().categoryName(), entry.getValue() );
                })

                .sorted(Comparator.comparing(CategorySpendDto::amount).reversed())
                .collect(Collectors.toList());
    }

    private SubscriptionDto mapToDto(Subscription s) {

        return new SubscriptionDto(
                s.getId(),
                s.getName(),
                s.getCost(),
                s.getBillingCycle(),
                s.getNextRenewalDate(),
                s.getCategory() != null ? s.getCategory().getName() : "Uncategorized",
                s.getPaymentMethod(),
                s.getNotes());
    }
}

