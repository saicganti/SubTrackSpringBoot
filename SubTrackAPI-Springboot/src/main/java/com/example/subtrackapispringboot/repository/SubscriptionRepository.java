package com.example.subtrackapispringboot.repository;

import com.example.subtrackapispringboot.entity.Subscription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @EntityGraph(attributePaths = "category")
    List<Subscription> findByUserIdOrderByNextRenewalDate(String userId);

    @EntityGraph(attributePaths = "category")
    List<Subscription> findByUserIdAndNextRenewalDateBetweenOrderByNextRenewalDate(
            String userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    @EntityGraph(attributePaths = "category")
    Subscription findByUserId(String userId);

    @EntityGraph
    Subscription findByIdAndUserId(Integer id, String userId);

    void deleteByIdAndUserId(Integer id, String userId);
}
