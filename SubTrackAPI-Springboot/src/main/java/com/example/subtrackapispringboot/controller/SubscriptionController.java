package com.example.subtrackapispringboot.controller;

import com.example.subtrackapispringboot.dto.CreateSubscriptionDto;
import com.example.subtrackapispringboot.dto.DashboardDto;
import com.example.subtrackapispringboot.dto.SubscriptionDto;
import com.example.subtrackapispringboot.entity.Subscription;
import com.example.subtrackapispringboot.service.SubscriptionService;
import org.hibernate.annotations.NotFound;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    private String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            return "";
        }

        Object principal = authentication.getPrincipal();
        if(principal instanceof jwt) {
            Jwt jwt = (Jwt) principal;
            String subject = jwt.getSubject();
            if(subject == null) {
                return "";
            }
            return subject;
        }

        String name = authentication.getName();
        if(name == null){
            return "";
        }
        return name;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getAll(Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";

        List<SubscriptionDto> result = service.getAll(userId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<SubscriptionDto> getById( @PathVariable("id") Integer id, Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";

        SubscriptionDto result = service.getById(id, userId);
        if(result == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> create(@RequestBody CreateSubscriptionDto dto, Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";
        SubscriptionDto result = service.create(dto, userId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(location).body(result);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<SubscriptionDto> update(@RequestBody CreateSubscriptionDto dto, @PathVariable("id") Integer id, Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";
        SubscriptionDto result = service.update(id, dto, userId);

        if(result == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("{id:\\d+}")
    public ResponseEntity<SubscriptionDto> delete(@PathVariable("id") Integer id, Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";
        boolean result = service.delete(id, userId);
        if(!result){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard(Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";
        DashboardDto result = service.getDashboard(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SubscriptionDto>> getUpcoming(@RequestParam(defaultValue = "30") int days, Principal principal){

        String userId = principal != null ? principal.getName() : "test-user";
        List<SubscriptionDto> result = service.getRenewals(userId, days);

        return ResponseEntity.ok(result);
    }
}
