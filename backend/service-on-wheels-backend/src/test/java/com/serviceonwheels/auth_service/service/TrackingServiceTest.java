package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.TrackingResponse;
import com.serviceonwheels.auth_service.model.ServiceRequest;
import com.serviceonwheels.auth_service.model.RequestStatus;
import com.serviceonwheels.auth_service.model.TrackingStatus;
import com.serviceonwheels.auth_service.model.User;
import com.serviceonwheels.auth_service.repository.ServiceRequestRepository;
import com.serviceonwheels.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackingService")
class TrackingServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TrackingService trackingService;

    private User mockUser;
    private ServiceRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("user-1");
        mockUser.setEmail("test@example.com");

        mockRequest = ServiceRequest.builder()
                .id("req-abc123")
                .userId("user-1")
                .vehicleType("Car")
                .vehicleNumber("KA01AB1234")
                .problemDescription("Flat tire")
                .latitude(12.9716)
                .longitude(77.5946)
                .status(RequestStatus.PENDING)
                .trackingStatus(TrackingStatus.ASSIGNED)
                .mechanicName("Ravi Kumar")
                .mechanicPhone("+91 98765 43210")
                .mechanicVehicle("Bike KA01AB1234")
                .mechanicRating(4.8)
                .mechanicStartLat(TrackingService.startLat(12.9716))
                .mechanicStartLng(TrackingService.startLng(77.5946))
                .assignedAt(LocalDateTime.now().minusSeconds(60))
                .createdAt(LocalDateTime.now().minusSeconds(65))
                .build();
    }

    // ── Deterministic Assignment ─────────────────────────

    @Nested
    @DisplayName("Mechanic Assignment")
    class MechanicAssignment {

        @Test
        @DisplayName("same request ID always returns the same mechanic index")
        void sameIdSameMechanic() {
            int idx1 = TrackingService.mechanicIndex("req-abc123");
            int idx2 = TrackingService.mechanicIndex("req-abc123");
            assertThat(idx1).isEqualTo(idx2);
        }

        @Test
        @DisplayName("different request IDs can return different mechanics")
        void differentIdsDifferentMechanics() {
            // With 5 mechanics, at least 2 of 10 random IDs should differ
            boolean anyDiffers = false;
            int first = TrackingService.mechanicIndex("id-0");
            for (int i = 1; i < 10; i++) {
                if (TrackingService.mechanicIndex("id-" + i) != first) {
                    anyDiffers = true;
                    break;
                }
            }
            assertThat(anyDiffers).isTrue();
        }

        @Test
        @DisplayName("mechanic index is always within pool bounds")
        void indexWithinBounds() {
            for (int i = 0; i < 100; i++) {
                int idx = TrackingService.mechanicIndex("test-" + i);
                assertThat(idx).isBetween(0, 4);
            }
        }

        @Test
        @DisplayName("pickName returns a non-blank name")
        void pickNameReturnsName() {
            String name = TrackingService.pickName("req-abc123");
            assertThat(name).isNotBlank();
        }
    }

    // ── Simulation Logic ─────────────────────────────────

    @Nested
    @DisplayName("Simulation")
    class Simulation {

        @Test
        @DisplayName("haversine distance between start and user is ~3.4 km for 0.022° offset")
        void haversineReasonable() {
            double dist = TrackingService.haversineMetres(
                    12.9716, 77.5946,
                    12.9716 + 0.022, 77.5946 + 0.022
            );
            // ~3.0-3.5 km for this offset at Bangalore latitude
            assertThat(dist).isBetween(2500.0, 4000.0);
        }

        @Test
        @DisplayName("tracking returns ON_THE_WAY after 30+ seconds")
        void statusOnTheWay() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(serviceRequestRepository.findById("req-abc123")).thenReturn(Optional.of(mockRequest));

            TrackingResponse resp = trackingService.getTracking("req-abc123", "test@example.com");

            assertThat(resp.getTrackingStatus()).isEqualTo(TrackingStatus.ON_THE_WAY);
            assertThat(resp.getMechanicName()).isEqualTo("Ravi Kumar");
        }

        @Test
        @DisplayName("tracking returns ARRIVED when enough time has passed")
        void statusArrived() {
            // Set assignedAt far in the past so mechanic has arrived
            mockRequest.setAssignedAt(LocalDateTime.now().minusMinutes(30));

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(serviceRequestRepository.findById("req-abc123")).thenReturn(Optional.of(mockRequest));

            TrackingResponse resp = trackingService.getTracking("req-abc123", "test@example.com");

            assertThat(resp.getTrackingStatus()).isEqualTo(TrackingStatus.ARRIVED);
            assertThat(resp.getEta()).isEqualTo("Arrived");
            assertThat(resp.getDistanceMeters()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("ETA decreases as time passes")
        void etaDecreases() {
            // 30 seconds ago
            mockRequest.setAssignedAt(LocalDateTime.now().minusSeconds(30));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(serviceRequestRepository.findById("req-abc123")).thenReturn(Optional.of(mockRequest));

            TrackingResponse resp1 = trackingService.getTracking("req-abc123", "test@example.com");

            // 120 seconds ago
            mockRequest.setAssignedAt(LocalDateTime.now().minusSeconds(120));
            TrackingResponse resp2 = trackingService.getTracking("req-abc123", "test@example.com");

            assertThat(resp2.getEtaSeconds()).isLessThan(resp1.getEtaSeconds());
        }
    }

    // ── Authorization ────────────────────────────────────

    @Nested
    @DisplayName("Authorization")
    class Authorization {

        @Test
        @DisplayName("throws ForbiddenException for wrong user")
        void wrongUserForbidden() {
            User otherUser = new User();
            otherUser.setId("user-other");
            otherUser.setEmail("other@example.com");

            when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
            when(serviceRequestRepository.findById("req-abc123")).thenReturn(Optional.of(mockRequest));

            assertThatThrownBy(() -> trackingService.getTracking("req-abc123", "other@example.com"))
                    .isInstanceOf(com.serviceonwheels.auth_service.exception.ForbiddenException.class);
        }

        @Test
        @DisplayName("throws ServiceRequestNotFoundException for invalid ID")
        void notFound() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
            when(serviceRequestRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> trackingService.getTracking("nonexistent", "test@example.com"))
                    .isInstanceOf(com.serviceonwheels.auth_service.exception.ServiceRequestNotFoundException.class);
        }
    }
}
