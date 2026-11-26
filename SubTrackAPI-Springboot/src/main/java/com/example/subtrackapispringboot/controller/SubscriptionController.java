package com.example.subtrackapispringboot.controller;

import com.example.subtrackapispringboot.dto.CreateSubscriptionDto;
import com.example.subtrackapispringboot.dto.DashboardDto;
import com.example.subtrackapispringboot.dto.SubscriptionDto;
import com.example.subtrackapispringboot.exception.ResourceNotFoundException;
import com.example.subtrackapispringboot.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAll(@AuthenticationPrincipal Integer userId) {
        return ResponseEntity.ok(service.getAll(String.valueOf(userId)));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SubscriptionDto> getById(@PathVariable("id") Integer id,
                                                   @AuthenticationPrincipal Integer userId) {
        SubscriptionDto result = service.getById(id, String.valueOf(userId));
        if (result == null) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> create(@RequestBody CreateSubscriptionDto dto,
                                                  @AuthenticationPrincipal Integer userId) {
        return ResponseEntity.ok(service.create(dto, String.valueOf(userId)));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<SubscriptionDto> update(@PathVariable("id") Integer id,
                                                  @RequestBody CreateSubscriptionDto dto,
                                                  @AuthenticationPrincipal Integer userId) {
        SubscriptionDto result = service.update(id, dto, String.valueOf(userId));
        if (result == null) {
            throw new ResourceNotFoundException("Subscription not found");
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id,
                                       @AuthenticationPrincipal Integer userId) {
        boolean deleted = service.delete(id, String.valueOf(userId));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal Integer userId) {
        return ResponseEntity.ok(service.getDashboard(String.valueOf(userId)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionDto>> getUpcoming(@AuthenticationPrincipal Integer userId,
                                                             @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getRenewals(String.valueOf(userId), days));
    }
}
