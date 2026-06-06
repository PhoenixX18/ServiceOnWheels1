package com.serviceonwheels.auth_service.model;

public enum RequestStatus {
    // Existing (preserved for backward compatibility)
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    TOW_REQUIRED,
    // Phase 4 lifecycle statuses
    ASSIGNED,
    ON_THE_WAY,
    ARRIVED,
    IN_SERVICE
}
