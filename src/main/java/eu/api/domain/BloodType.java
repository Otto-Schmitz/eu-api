package eu.api.domain;

import java.util.Arrays;

public enum BloodType {
    A_PLUS("A+"),
    A_MINUS("A-"),
    B_PLUS("B+"),
    B_MINUS("B-"),
    AB_PLUS("AB+"),
    AB_MINUS("AB-"),
    O_PLUS("O+"),
    O_MINUS("O-"),
    UNKNOWN("UNKNOWN");

    private final String code;

    BloodType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static BloodType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return UNKNOWN;
        }
        String normalized = code.trim().toUpperCase();
        return Arrays.stream(values())
                .filter(b -> b.code.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid blood type: " + code));
    }
}
