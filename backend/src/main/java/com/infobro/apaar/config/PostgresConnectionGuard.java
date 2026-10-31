package com.infobro.apaar.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Arrays;

/** Validate before constructing a datasource; never include the URL in errors. */
public class PostgresConnectionGuard implements EnvironmentPostProcessor, Ordered {
    // Public project identifier, deliberately pinned; this is not a credential.
    static final String APPROVED_DEMO_PROJECT = "hxmlezblzvdbacebsmcb";
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("render"))) {
            if (!environment.acceptsProfiles(Profiles.of("postgres & !dev & !h2")))
                throw new IllegalStateException("Render requires postgres without dev or h2; no database fallback is permitted.");
            requireCloudOrigins(environment.getProperty("apaar.cors.allowed-origins"));
            try {
                if (java.util.Base64.getDecoder().decode(environment.getProperty("apaar.jwt.secret", "")).length < 48)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Render requires JWT_SECRET containing at least 48 Base64-encoded random bytes.");
            }
        }
        if (environment.acceptsProfiles(Profiles.of("postgres & !dev"))) {
            for (String key : List.of("spring.datasource.url", "spring.datasource.username", "spring.flyway.user")) {
                if (environment.getProperty(key, "").contains("bbrkqbhjuiuewraokuom"))
                    throw new IllegalStateException("The retired production-labelled Supabase project is blocked. Configure the new separate demo project.");
            }
            requireTls(environment.getProperty("spring.datasource.url"));
            requireDemoTarget(environment.getProperty("SUPABASE_PROJECT_REF"),
                environment.getProperty("spring.datasource.url"), environment.getProperty("spring.datasource.username"),
                environment.getProperty("spring.flyway.user"));
            if (!"validate".equals(environment.getProperty("spring.jpa.hibernate.ddl-auto")))
                throw new IllegalStateException("PostgreSQL requires Hibernate ddl-auto=validate; schema creation/update is forbidden.");
            if (!environment.getProperty("spring.flyway.enabled", Boolean.class, false))
                throw new IllegalStateException("PostgreSQL requires Flyway enabled; no H2 fallback is permitted.");
        }
    }
    static void requireDemoTarget(String project, String jdbc, String appUser, String migrationUser) {
        if (!APPROVED_DEMO_PROJECT.equals(project))
            throw new IllegalStateException("SUPABASE_PROJECT_REF must match the explicitly approved new demo project. No other project is permitted.");
        try {
            if (jdbc == null || !jdbc.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException();
            java.net.URI uri = java.net.URI.create(jdbc.substring(5));
            if (!"aws-0-ap-south-1.pooler.supabase.com".equals(uri.getHost()) || uri.getPort() != 5432
                || uri.getUserInfo() != null || uri.getFragment() != null || !"/postgres".equals(uri.getPath())
                || appUser == null || !appUser.endsWith("." + project)
                || migrationUser == null || !migrationUser.endsWith("." + project)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Render requires the new demo Session Pooler on port 5432, with both database identities matching SUPABASE_PROJECT_REF.");
        }
    }
    static void requireCloudOrigins(String origins) {
        if (origins == null || origins.isBlank()) throw new IllegalStateException("Render requires explicit HTTPS CORS_ALLOWED_ORIGINS.");
        for (String origin : origins.split(",", -1)) {
            try {
                java.net.URI uri = java.net.URI.create(origin.trim());
                String host = uri.getHost();
                if (!"https".equals(uri.getScheme()) || host == null || host.contains("*")
                    || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0") || host.startsWith("127.") || host.contains(":")
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                    || (uri.getPath() != null && !uri.getPath().isEmpty())) throw new IllegalArgumentException();
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Render CORS origins must be exact public HTTPS origins without wildcard, path, query or credentials.");
            }
        }
    }
    static void requireTls(String url) {
        if (url == null || !url.startsWith("jdbc:postgresql:") || !url.contains("?"))
            throw new IllegalStateException("DB_URL must be a PostgreSQL JDBC URL with sslmode=require");
        List<String> parameters = Arrays.stream(url.substring(url.indexOf('?') + 1).split("&"))
            .map(p -> URLDecoder.decode(p, StandardCharsets.UTF_8)).toList();
        List<String> modes = parameters.stream().filter(p -> p.startsWith("sslmode=")).toList();
        if (parameters.stream().anyMatch(p -> p.toLowerCase(java.util.Locale.ROOT).matches("(user|password|sslpassword|sslkey|token|apikey|api_key|access_token)=.*")))
            throw new IllegalStateException("Keep credentials in separate environment variables, not DB_URL query parameters.");
        if (!modes.equals(List.of("sslmode=require")) || parameters.stream().anyMatch(p ->
            p.toLowerCase().startsWith("sslfactory=") || p.equalsIgnoreCase("ssl=false")))
            throw new IllegalStateException("DB_URL must contain exactly one sslmode=require and no TLS override");
    }
}
