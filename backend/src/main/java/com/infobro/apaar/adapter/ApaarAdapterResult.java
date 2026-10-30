package com.infobro.apaar.adapter;

import java.time.LocalDate;

public class ApaarAdapterResult {
    public enum Status {
        VERIFIED,
        NOT_FOUND,
        NAME_MISMATCH,
        DOB_MISMATCH,
        INACTIVE,
        SERVICE_UNAVAILABLE
    }

    private final boolean success;
    private final Status status;
    private final String apaarId;
    private final String verifiedName;
    private final LocalDate verifiedDob;
    private final String message;

    public ApaarAdapterResult(boolean success, Status status, String apaarId, String verifiedName, LocalDate verifiedDob, String message) {
        this.success = success;
        this.status = status;
        this.apaarId = apaarId;
        this.verifiedName = verifiedName;
        this.verifiedDob = verifiedDob;
        this.message = message;
    }

    public static ApaarAdapterResult verified(String apaarId, String name, LocalDate dob) {
        return new ApaarAdapterResult(true, Status.VERIFIED, apaarId, name, dob, "APAAR ID verified successfully against ABC registry (Simulated)");
    }

    public static ApaarAdapterResult notFound(String apaarId) {
        return new ApaarAdapterResult(false, Status.NOT_FOUND, apaarId, null, null, "APAAR ID not found in Academic Bank of Credits registry (Simulated)");
    }

    public static ApaarAdapterResult mismatch(String apaarId, String expectedName, String verifiedName) {
        return new ApaarAdapterResult(false, Status.NAME_MISMATCH, apaarId, verifiedName, null,
                "Demographic mismatch: Provided name '" + expectedName + "' does not match registry record '" + verifiedName + "' (Simulated)");
    }

    public boolean isSuccess() { return success; }
    public Status getStatus() { return status; }
    public String getApaarId() { return apaarId; }
    public String getVerifiedName() { return verifiedName; }
    public LocalDate getVerifiedDob() { return verifiedDob; }
    public String getMessage() { return message; }
}
