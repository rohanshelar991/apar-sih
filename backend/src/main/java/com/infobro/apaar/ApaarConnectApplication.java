package com.infobro.apaar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

// Authentication uses AuthService + JWT; do not generate/log an unused fallback password.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableAsync
public class ApaarConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApaarConnectApplication.class, args);
    }
}
