package com.serviceonwheels.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "service_requests")
public class ServiceRequest {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String vehicleType;

    private String vehicleNumber;

    private String problemDescription;

    private Double latitude;

    private Double longitude;

    private RequestStatus status;

    private String assignedMechanicId;

    @CreatedDate
    private LocalDateTime createdAt;
}
