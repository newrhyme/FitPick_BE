package com.fitpick.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 50) String loginId,
        @NotBlank String password,
        @NotBlank @Size(max = 50) String name,
        String phone,
        Integer height,
        Integer weight,
        String ageGroup,
        String address
) {
}
