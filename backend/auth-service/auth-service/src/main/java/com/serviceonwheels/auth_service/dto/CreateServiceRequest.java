package com.serviceonwheels.auth_service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateServiceRequest {

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 80, message = "Vehicle type must be at most 80 characters")
    private String vehicleType;

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 32, message = "Vehicle number must be at most 32 characters")
    private String vehicleNumber;

    @NotBlank(message = "Problem description is required")
    @Size(max = 4000, message = "Problem description must be at most 4000 characters")
    private String problemDescription;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
}
