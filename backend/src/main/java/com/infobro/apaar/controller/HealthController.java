package com.infobro.apaar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/** Read-only readiness; never returns connection details or exception messages. */
@RestController
public class HealthController {
    private final DataSource dataSource;
    public HealthController(DataSource dataSource) { this.dataSource = dataSource; }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(3)) return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (Exception ignored) { }
        return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
    }
}
