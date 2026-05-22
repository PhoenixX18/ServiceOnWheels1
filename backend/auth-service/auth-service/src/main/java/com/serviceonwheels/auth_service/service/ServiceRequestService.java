package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.CreateServiceRequest;
import com.serviceonwheels.auth_service.dto.ServiceRequestResponse;
import com.serviceonwheels.auth_service.exception.ForbiddenException;
import com.serviceonwheels.auth_service.exception.ServiceRequestNotFoundException;
import com.serviceonwheels.auth_service.exception.UserNotFoundException;
import com.serviceonwheels.auth_service.model.RequestStatus;
import com.serviceonwheels.auth_service.model.ServiceRequest;
import com.serviceonwheels.auth_service.repository.ServiceRequestRepository;
import com.serviceonwheels.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    public ServiceRequestResponse create(CreateServiceRequest dto, String email) {
        String userId = resolveUserId(email);
        ServiceRequest entity = ServiceRequest.builder()
                .userId(userId)
                .vehicleType(dto.getVehicleType().trim())
                .vehicleNumber(dto.getVehicleNumber().trim())
                .problemDescription(dto.getProblemDescription().trim())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .status(RequestStatus.PENDING)
                .assignedMechanicId(null)
                .build();
        ServiceRequest saved = serviceRequestRepository.save(entity);
        return toResponse(saved);
    }

    public List<ServiceRequestResponse> listMine(String email) {
        String userId = resolveUserId(email);
        return serviceRequestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ServiceRequestResponse updateStatus(String serviceRequestId, RequestStatus newStatus, String email) {
        String userId = resolveUserId(email);
        ServiceRequest entity = serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException("Service request not found."));

        if (!userId.equals(entity.getUserId())) {
            throw new ForbiddenException("You can only update your own service requests.");
        }

        entity.setStatus(newStatus);
        return toResponse(serviceRequestRepository.save(entity));
    }

    private String resolveUserId(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."))
                .getId();
    }

    private ServiceRequestResponse toResponse(ServiceRequest entity) {
        return ServiceRequestResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .vehicleType(entity.getVehicleType())
                .vehicleNumber(entity.getVehicleNumber())
                .problemDescription(entity.getProblemDescription())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .status(entity.getStatus())
                .assignedMechanicId(entity.getAssignedMechanicId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
