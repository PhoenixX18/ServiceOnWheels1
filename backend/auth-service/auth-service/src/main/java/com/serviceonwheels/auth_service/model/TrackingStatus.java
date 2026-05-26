package com.serviceonwheels.auth_service.model;

/**
 * Represents the lifecycle of mechanic tracking for a service request.
 * Separate from {@link RequestStatus} to avoid breaking existing request flow.
 */
public enum TrackingStatus {
    PENDING,
    ASSIGNED,
    ON_THE_WAY,
    ARRIVED,
    COMPLETED
}
