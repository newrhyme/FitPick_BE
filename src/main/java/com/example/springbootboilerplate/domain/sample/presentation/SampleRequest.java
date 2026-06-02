package com.example.springbootboilerplate.domain.sample.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleRequest {

    @NotBlank(message = "name은 필수입니다!")
    private String name;
}
