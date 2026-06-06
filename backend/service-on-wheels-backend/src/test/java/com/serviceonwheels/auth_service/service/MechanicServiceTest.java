package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.MechanicResponse;
import com.serviceonwheels.auth_service.exception.MechanicNotFoundException;
import com.serviceonwheels.auth_service.model.Mechanic;
import com.serviceonwheels.auth_service.model.MechanicStatus;
import com.serviceonwheels.auth_service.repository.MechanicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MechanicServiceTest {

    @Mock
    private MechanicRepository mechanicRepository;

    @InjectMocks
    private MechanicService mechanicService;

    private Mechanic mechanic;

    @BeforeEach
    void setUp() {
        mechanic = Mechanic.builder()
                .id("mech-1")
                .name("Test Mechanic")
                .status(MechanicStatus.AVAILABLE)
                .build();
    }

    @Test
    void findAvailable_returnsOnlyAvailableMechanics() {
        when(mechanicRepository.findByStatus(MechanicStatus.AVAILABLE)).thenReturn(List.of(mechanic));

        List<Mechanic> available = mechanicService.findAvailable();

        assertEquals(1, available.size());
        assertEquals("mech-1", available.get(0).getId());
        verify(mechanicRepository).findByStatus(MechanicStatus.AVAILABLE);
    }

    @Test
    void assignToRequest_setsBusyAndActiveRequestId() {
        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(mechanicRepository.save(any(Mechanic.class))).thenAnswer(i -> i.getArgument(0));

        Mechanic assigned = mechanicService.assignToRequest("mech-1", "req-1");

        assertEquals(MechanicStatus.BUSY, assigned.getStatus());
        assertEquals("req-1", assigned.getActiveRequestId());
        verify(mechanicRepository).save(mechanic);
    }

    @Test
    void assignToRequest_mechanicNotFound_throwsException() {
        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.empty());

        assertThrows(MechanicNotFoundException.class, () -> mechanicService.assignToRequest("mech-1", "req-1"));
    }

    @Test
    void releaseFromRequest_setsAvailableAndClearsRequestId() {
        mechanic.setStatus(MechanicStatus.BUSY);
        mechanic.setActiveRequestId("req-1");

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(mechanicRepository.save(any(Mechanic.class))).thenAnswer(i -> i.getArgument(0));

        mechanicService.releaseFromRequest("mech-1");

        ArgumentCaptor<Mechanic> captor = ArgumentCaptor.forClass(Mechanic.class);
        verify(mechanicRepository).save(captor.capture());
        
        Mechanic saved = captor.getValue();
        assertEquals(MechanicStatus.AVAILABLE, saved.getStatus());
        assertNull(saved.getActiveRequestId());
    }

    @Test
    void releaseFromRequest_mechanicNotFound_throwsException() {
        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.empty());

        assertThrows(MechanicNotFoundException.class, () -> mechanicService.releaseFromRequest("mech-1"));
    }
}
