package com.infobro.apaar.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Simulated APAAR Verification Adapter.
 * NOTICE: Smart India Hackathon Prototype - Uses synthetic verification logic.
 * Never connects to actual UIDAI/ABC live services.
 */
@Component
public class SimulatedApaarVerificationAdapter implements ApaarVerificationAdapter {

    private static final Logger log = LoggerFactory.getLogger(SimulatedApaarVerificationAdapter.class);

    @Value("${apaar.adapter.simulation-delay-ms:20}")
    private long simulationDelayMs;

    @Value("${apaar.adapter.unknown-apaar-prefix:9999}")
    private String unknownPrefix;

    // Configurable synthetic set of non-existent APAAR IDs for demo scenarios
    private final Set<String> knownNonExistentIds = new HashSet<>(Arrays.asList(
        "999900001111",
        "999900002222",
        "999999999999",
        "888800000000"
    ));

    @Override
    public ApaarAdapterResult verifyApaar(String apaarId, String providedName, LocalDate providedDob) {
        if (simulationDelayMs > 0) {
            try {
                Thread.sleep(Math.min(simulationDelayMs, 100));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (apaarId == null) {
            return ApaarAdapterResult.notFound("null");
        }

        String normalizedId = apaarId.replaceAll("[\\s-]+", "");

        // Check if ID is in non-existent synthetic test set or starts with unknown prefix
        if (normalizedId.startsWith(unknownPrefix) || knownNonExistentIds.contains(normalizedId)) {
            log.info("[SIMULATED-APAAR] APAAR ID {} identified as not found in synthetic registry", normalizedId);
            return ApaarAdapterResult.notFound(normalizedId);
        }

        // Demo rule: if ID ends with "0000", simulate demographic mismatch
        if (normalizedId.endsWith("0000") && providedName != null && !providedName.contains("DemoMismatchExpected")) {
            return ApaarAdapterResult.mismatch(normalizedId, providedName, "Synthesized Registry Record " + normalizedId.substring(8));
        }

        return ApaarAdapterResult.verified(normalizedId, providedName, providedDob);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
