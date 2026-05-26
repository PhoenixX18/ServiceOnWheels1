package com.serviceonwheels.auth_service.controller;

import com.serviceonwheels.auth_service.dto.TrackingResponse;
import com.serviceonwheels.auth_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST controller for real-time mechanic tracking.
 * Separated from {@link ServiceRequestController} to keep concerns isolated.
 */
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    /**
     * Returns the current tracking state for a service request.
     * Polled every 5 seconds by the Angular tracking page.
     *
     * @param requestId the service request ID
     * @param principal the authenticated user
     * @return tracking response with mechanic position, ETA, and status
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<TrackingResponse> getTracking(
            @PathVariable("requestId") String requestId,
            Principal principal) {
        TrackingResponse response = trackingService.getTracking(requestId, principal.getName());
        return ResponseEntity.ok(response);
    }
}
