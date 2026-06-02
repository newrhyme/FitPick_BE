package com.example.springbootboilerplate.domain.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @Value("${app.profile-name:unknown}")
    private String profileName;

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        return Map.of(
                "status",
                "ok",
                "profile", profileName
        );
    }
}
