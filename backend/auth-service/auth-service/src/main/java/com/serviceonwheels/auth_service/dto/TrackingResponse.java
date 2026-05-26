package com.serviceonwheels.auth_service.dto;

import com.serviceonwheels.auth_service.model.TrackingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for the GET /api/tracking/{requestId} endpoint.
 * Contains all data needed to render the Swiggy/Uber-style tracking page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private String requestId;
    private TrackingStatus trackingStatus;

    // ── Request info ─────────────────────────────────────
    private String vehicleType;
    private String vehicleNumber;
    private String problemDescription;
    private String selectedIssue;
    private String additionalNotes;
    private String address;

    // ── ETA & distance ───────────────────────────────────
    private String eta;
    private String distanceRemaining;
    private long etaSeconds;
    private double distanceMeters;

    // ── User location ────────────────────────────────────
    private double userLat;
    private double userLng;

    // ── Mechanic current (simulated) location ────────────
    private double mechanicLat;
    private double mechanicLng;

    // ── Mechanic details ─────────────────────────────────
    private String mechanicName;
    private String mechanicPhone;
    private String mechanicVehicle;
    private double mechanicRating;
}
