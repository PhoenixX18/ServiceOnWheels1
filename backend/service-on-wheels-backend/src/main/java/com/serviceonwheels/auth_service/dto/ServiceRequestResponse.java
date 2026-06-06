package com.serviceonwheels.auth_service.dto;

import com.serviceonwheels.auth_service.model.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestResponse {

    private String id;
    private String userId;
    private String vehicleType;
    private String vehicleNumber;
    private String problemDescription;
    private String selectedIssue;
    private String additionalNotes;
    private Double latitude;
    private Double longitude;
    private String address;
    private RequestStatus status;
    private String assignedMechanicId;
    private LocalDateTime createdAt;

    // ── Mechanic details (Phase 4) ───────────────────────
    private String mechanicName;
    private String mechanicPhone;
    private String mechanicVehicle;
    private Double mechanicRating;

    // ── Audit timestamps (Phase 4) ───────────────────────
    private LocalDateTime assignedAt;
    private LocalDateTime arrivedAt;
    private LocalDateTime serviceStartedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
}

