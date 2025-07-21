package com.islandhop.reviewservice.enums;

public enum ReviewStatus {
    PENDING(0),
    APPROVED(1),
    REJECTED(2),
    TO_SUPPORT_AGENTS(3);

    private final int value;

    ReviewStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ReviewStatus fromValue(int value) {
        for (ReviewStatus status : ReviewStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }
}