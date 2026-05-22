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
    private Double latitude;
    private Double longitude;
    private RequestStatus status;
    private String assignedMechanicId;
    private LocalDateTime createdAt;
}
