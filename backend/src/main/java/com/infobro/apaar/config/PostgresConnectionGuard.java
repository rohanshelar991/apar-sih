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
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.acceptsProfiles(Profiles.of("postgres & !dev")))
            requireTls(environment.getProperty("spring.datasource.url"));
    }
    static void requireTls(String url) {
        if (url == null || !url.startsWith("jdbc:postgresql:") || !url.contains("?"))
            throw new IllegalStateException("DB_URL must be a PostgreSQL JDBC URL with sslmode=require");
        List<String> parameters = Arrays.stream(url.substring(url.indexOf('?') + 1).split("&"))
            .map(p -> URLDecoder.decode(p, StandardCharsets.UTF_8)).toList();
        List<String> modes = parameters.stream().filter(p -> p.startsWith("sslmode=")).toList();
        if (!modes.equals(List.of("sslmode=require")) || parameters.stream().anyMatch(p ->
            p.toLowerCase().startsWith("sslfactory=") || p.equalsIgnoreCase("ssl=false")))
            throw new IllegalStateException("DB_URL must contain exactly one sslmode=require and no TLS override");
    }
}
