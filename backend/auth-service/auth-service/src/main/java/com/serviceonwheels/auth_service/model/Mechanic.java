package com.serviceonwheels.auth_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a mechanic in the system.
 * Stored in the {@code mechanics} MongoDB collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "mechanics")
public class Mechanic {

    @Id
    private String id;

    private String name;

    private String phone;

    private Double rating;

    private String vehicle;

    private MechanicStatus status;

    private Double currentLat;

    private Double currentLng;

    /** The ID of the service request this mechanic is currently assigned to. */
    private String activeRequestId;
}
