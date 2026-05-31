package com.serviceonwheels.auth_service.dto;

import com.serviceonwheels.auth_service.model.MechanicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MechanicResponse {

    private String id;
    private String name;
    private String phone;
    private Double rating;
    private String vehicle;
    private MechanicStatus status;
    private Double currentLat;
    private Double currentLng;
    private String activeRequestId;
}
