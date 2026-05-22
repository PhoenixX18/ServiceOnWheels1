package com.serviceonwheels.auth_service.controller;

import com.serviceonwheels.auth_service.dto.CreateServiceRequest;
import com.serviceonwheels.auth_service.dto.ServiceRequestResponse;
import com.serviceonwheels.auth_service.model.RequestStatus;
import com.serviceonwheels.auth_service.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/service")
@RequiredArgsConstructor
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @PostMapping("/request")
    public ResponseEntity<ServiceRequestResponse> create(
            @Valid @RequestBody CreateServiceRequest body,
            Principal principal) {
        ServiceRequestResponse response = serviceRequestService.create(body, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ServiceRequestResponse>> myRequests(Principal principal) {
        return ResponseEntity.ok(serviceRequestService.listMine(principal.getName()));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<ServiceRequestResponse> updateStatus(
            @PathVariable("id") String id,
            @RequestParam("status") RequestStatus status,
            Principal principal) {
        return ResponseEntity.ok(serviceRequestService.updateStatus(id, status, principal.getName()));
    }
}
