package com.infobro.apaar.adapter;

import java.time.LocalDate;

/**
 * Adapter interface for APAAR / Academic Bank of Credits (ABC) verification.
 * In production, a secure government API gateway implementation will implement this interface.
 * For this prototype, SimulatedApaarVerificationAdapter is used with synthetic data.
 */
public interface ApaarVerificationAdapter {
    ApaarAdapterResult verifyApaar(String apaarId, String providedName, LocalDate providedDob);
    boolean isAvailable();
}
