package com.infobro.apaar.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class PostgresConnectionGuardTest {
    @Test void deploymentPinsBothDatabaseIdentitiesToTheNewProjectAndRejectsTheOldOne() {
        String project="hxmlezblzvdbacebsmcb";
        String url="jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require";
        assertDoesNotThrow(() -> PostgresConnectionGuard.requireDemoTarget(project,url,"apaar_app."+project,"apaar_migrator."+project));
        assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireDemoTarget("abcdefghijklmnopqrst",url,"apaar_app.abcdefghijklmnopqrst","apaar_migrator.abcdefghijklmnopqrst"));
        assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireDemoTarget(project,url.replace(":5432/",":6543/"),"apaar_app."+project,"apaar_migrator."+project));
        assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireDemoTarget("bbrkqbhjuiuewraokuom",url,"apaar_app.bbrkqbhjuiuewraokuom","apaar_migrator.bbrkqbhjuiuewraokuom"));
        assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireDemoTarget(project,url,"apaar_app."+project,"apaar_migrator.bbrkqbhjuiuewraokuom"));
        assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireDemoTarget(project,"jdbc:postgresql://localhost:5432/demo?sslmode=require","apaar_app."+project,"apaar_migrator."+project));
    }
    @Test void cloudOriginsRejectLocalWildcardAndCredentialBearingUrls() {
        for (String origin : new String[]{"", "*", "http://frontend.example.com", "https://localhost", "https://127.0.0.1", "https://*.vercel.app", "https://user:password@example.com", "https://example.com/path", "https://example.com?secret=x"})
            assertThrows(IllegalStateException.class, () -> PostgresConnectionGuard.requireCloudOrigins(origin));
        assertDoesNotThrow(() -> PostgresConnectionGuard.requireCloudOrigins("https://frontend.example.com,https://frontend.vercel.app"));
    }
    @Test void renderRejectsH2AndMissingSecrets() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("render", "h2");
        PostgresConnectionGuard guard = new PostgresConnectionGuard();
        assertThrows(IllegalStateException.class, () -> guard.postProcessEnvironment(env, new SpringApplication()));
        env.setActiveProfiles("render", "postgres");
        env.setProperty("apaar.cors.allowed-origins", "https://frontend.example.com");
        env.setProperty("apaar.jwt.secret", "short");
        assertThrows(IllegalStateException.class, () -> guard.postProcessEnvironment(env, new SpringApplication()));
    }
    @Test void rejectsUnsafeSchemaModesAndDisabledFlyway() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("postgres");
        env.setProperty("spring.datasource.url", "jdbc:postgresql://aws-0-ap-south-1.pooler.supabase.com:5432/postgres?sslmode=require");
        env.setProperty("SUPABASE_PROJECT_REF", "hxmlezblzvdbacebsmcb");
        env.setProperty("spring.datasource.username", "apaar_app.hxmlezblzvdbacebsmcb");
        env.setProperty("spring.flyway.user", "apaar_migrator.hxmlezblzvdbacebsmcb");
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
