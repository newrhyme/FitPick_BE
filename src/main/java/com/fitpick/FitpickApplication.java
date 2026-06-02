package com.fitpick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FitpickApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitpickApplication.class, args);
    }

}
