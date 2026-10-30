package com.infobro.apaar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApaarConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApaarConnectApplication.class, args);
    }
}
