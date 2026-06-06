package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.MechanicResponse;
import com.serviceonwheels.auth_service.exception.MechanicNotFoundException;
import com.serviceonwheels.auth_service.model.Mechanic;
import com.serviceonwheels.auth_service.model.MechanicStatus;
import com.serviceonwheels.auth_service.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for mechanic operations: listing, availability checks,
 * assignment, and release.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository mechanicRepository;

    public List<MechanicResponse> listAll() {
        return mechanicRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MechanicResponse findById(String id) {
        Mechanic mechanic = mechanicRepository.findById(id)
                .orElseThrow(() -> new MechanicNotFoundException("Mechanic not found with id: " + id));
        return toResponse(mechanic);
    }

    public List<Mechanic> findAvailable() {
        return mechanicRepository.findByStatus(MechanicStatus.AVAILABLE);
    }

    /**
     * Assign a mechanic to a request: sets status to BUSY and records the activeRequestId.
     */
    public Mechanic assignToRequest(String mechanicId, String requestId) {
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new MechanicNotFoundException("Mechanic not found with id: " + mechanicId));

        mechanic.setStatus(MechanicStatus.BUSY);
        mechanic.setActiveRequestId(requestId);
        Mechanic saved = mechanicRepository.save(mechanic);
        log.info("Mechanic [{}] assigned to request [{}]", mechanicId, requestId);
        return saved;
    }

    /**
     * Release a mechanic from their current request: sets status back to AVAILABLE.
     */
    public void releaseFromRequest(String mechanicId) {
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new MechanicNotFoundException("Mechanic not found with id: " + mechanicId));

        mechanic.setStatus(MechanicStatus.AVAILABLE);
        mechanic.setActiveRequestId(null);
        mechanicRepository.save(mechanic);
        log.info("Mechanic [{}] released from active request", mechanicId);
    }

    private MechanicResponse toResponse(Mechanic mechanic) {
        return MechanicResponse.builder()
                .id(mechanic.getId())
                .name(mechanic.getName())
                .phone(mechanic.getPhone())
                .rating(mechanic.getRating())
                .vehicle(mechanic.getVehicle())
                .status(mechanic.getStatus())
                .currentLat(mechanic.getCurrentLat())
                .currentLng(mechanic.getCurrentLng())
                .activeRequestId(mechanic.getActiveRequestId())
                .build();
    }
}
