package com.example.foodordering.controller;

import com.example.foodordering.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .service("Online Food Ordering System API")
                .timestamp(Instant.now())
                .message("Service is running smoothly")
                .build();
        return ResponseEntity.ok(response);
    }
}
