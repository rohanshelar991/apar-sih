package com.infobro.apaar.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class PostgresConnectionGuardTest {
    @Test void rejectsUnsafeSchemaModesAndDisabledFlyway() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("postgres");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://host/db?sslmode=require");
        env.setProperty("spring.flyway.enabled", "true");
        PostgresConnectionGuard guard = new PostgresConnectionGuard();
        for (String mode : new String[]{"update", "create", "create-drop", "none"}) {
            env.setProperty("spring.jpa.hibernate.ddl-auto", mode);
            assertThrows(IllegalStateException.class, () -> guard.postProcessEnvironment(env, new SpringApplication()));
        }
        env.setProperty("spring.jpa.hibernate.ddl-auto", "validate");
        assertDoesNotThrow(() -> guard.postProcessEnvironment(env, new SpringApplication()));
        env.setProperty("spring.flyway.enabled", "false");
        assertThrows(IllegalStateException.class, () -> guard.postProcessEnvironment(env, new SpringApplication()));
    }
    @Test void rejectsMissingOrConflictingTls() {
        for (String url : new String[]{"jdbc:postgresql://localhost/db", "jdbc:postgresql://localhost/db?sslmode=disable",
            "jdbc:postgresql://localhost/db?sslmode=require&sslmode=disable"})
            assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireTls(url));
        assertDoesNotThrow(() -> PostgresConnectionGuard.requireTls("jdbc:postgresql://host/db?sslmode=require"));
    }
    @Test void devExemptionIsExplicit() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("postgres");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://localhost/db");
        PostgresConnectionGuard guard = new PostgresConnectionGuard();
        assertThrows(IllegalStateException.class, () -> guard.postProcessEnvironment(env, new SpringApplication()));
        env.setActiveProfiles("postgres", "dev");
        assertDoesNotThrow(() -> guard.postProcessEnvironment(env, new SpringApplication()));
    }
}
