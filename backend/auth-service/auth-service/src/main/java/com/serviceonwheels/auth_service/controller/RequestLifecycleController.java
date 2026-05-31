package com.serviceonwheels.auth_service.controller;

import com.serviceonwheels.auth_service.dto.ServiceRequestResponse;
import com.serviceonwheels.auth_service.service.RequestLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for request lifecycle actions.
 * Maps each mechanic workflow step to a dedicated endpoint.
 *
 * <p>Think: Uber driver actions — accept, start trip, arrived,
 * begin service, complete, cancel.</p>
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestLifecycleController {

    private final RequestLifecycleService lifecycleService;

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN', 'USER')")
    public ResponseEntity<ServiceRequestResponse> accept(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.acceptRequest(id));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN', 'USER')")
    public ResponseEntity<ServiceRequestResponse> startTrip(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.startTrip(id));
    }

    @PutMapping("/{id}/arrived")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN', 'USER')")
    public ResponseEntity<ServiceRequestResponse> arrived(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.markArrived(id));
    }

    @PutMapping("/{id}/service-start")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN', 'USER')")
    public ResponseEntity<ServiceRequestResponse> serviceStart(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.beginService(id));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MECHANIC', 'ADMIN', 'USER')")
    public ResponseEntity<ServiceRequestResponse> complete(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.completeService(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'MECHANIC', 'ADMIN')")
    public ResponseEntity<ServiceRequestResponse> cancel(@PathVariable("id") String id) {
        return ResponseEntity.ok(lifecycleService.cancelRequest(id));
    }
}
