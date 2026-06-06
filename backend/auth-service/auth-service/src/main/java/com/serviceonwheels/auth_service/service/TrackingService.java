package com.serviceonwheels.auth_service.service;

import com.serviceonwheels.auth_service.dto.TrackingResponse;
import com.serviceonwheels.auth_service.exception.ForbiddenException;
import com.serviceonwheels.auth_service.exception.ServiceRequestNotFoundException;
import com.serviceonwheels.auth_service.model.ServiceRequest;
import com.serviceonwheels.auth_service.model.TrackingStatus;
import com.serviceonwheels.auth_service.repository.ServiceRequestRepository;
import com.serviceonwheels.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Core tracking logic: mechanic pool, deterministic assignment,
 * and simulated movement along a straight-line path.
 *
 * <p>The simulation is deterministic: given the same {@code requestId} and
 * wall-clock time, every caller computes the identical mechanic position.
 * This avoids storing mutable GPS state and makes the system future-ready
 * for real GPS (just swap the position source).</p>
 */
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    // ── Mechanic Pool ────────────────────────────────────
    private static final String[] MECHANIC_NAMES = {
            "Ravi Kumar", "Arjun Patel", "Suresh R", "Meena Devi", "Karthik S"
    };
    private static final String[] MECHANIC_PHONES = {
            "+91 98765 43210", "+91 98765 43211", "+91 98765 43212",
            "+91 98765 43213", "+91 98765 43214"
    };
    private static final String[] MECHANIC_VEHICLES = {
            "Bike KA01AB1234", "Van KA02CD5678", "Tow Truck KA03EF9012",
            "Bike KA04GH3456", "Van KA05IJ7890"
    };
    private static final double[] MECHANIC_RATINGS = {4.8, 4.7, 4.9, 4.6, 4.8};

    /** Simulated travel speed in metres per second (~40 km/h). */
    private static final double SPEED_MPS = 40_000.0 / 3600.0; // ≈ 11.11 m/s

    /** Offset in degrees (~2.5 km) used to place the mechanic's start position. */
    private static final double START_OFFSET_DEG = 0.022;

    // ── Public API ───

    /**
     * Build a {@link TrackingResponse} for the given request.
     *
     * @param requestId the service request ID
     * @param email     the authenticated user's email (for ownership check)
     * @return fully populated tracking response
     */
    public TrackingResponse getTracking(String requestId, String email) {
        String userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User not found."))
                .getId();

        ServiceRequest req = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException("Service request not found."));

        if (!userId.equals(req.getUserId())) {
            throw new ForbiddenException("You can only track your own service requests.");
        }

        if (req.getAssignedAt() == null) {
            // Not yet assigned — return PENDING status
            return TrackingResponse.builder()
                    .requestId(requestId)
                    .trackingStatus(TrackingStatus.PENDING)
                    .vehicleType(req.getVehicleType())
                    .vehicleNumber(req.getVehicleNumber())
                    .problemDescription(req.getProblemDescription())
                    .userLat(req.getLatitude())
                    .userLng(req.getLongitude())
                    .eta("Assigning...")
                    .distanceRemaining("--")
                    .build();
        }

        return computeTracking(req);
    }

    // ── Mechanic Assignment (called by ServiceRequestService) ─

    /**
     * Deterministically select a mechanic index from the pool.
     * Same requestId → same mechanic, always.
     */
    public static int mechanicIndex(String requestId) {
        return Math.abs(requestId.hashCode()) % MECHANIC_NAMES.length;
    }

    public static String pickName(String requestId) {
        return MECHANIC_NAMES[mechanicIndex(requestId)];
    }

    public static String pickPhone(String requestId) {
        return MECHANIC_PHONES[mechanicIndex(requestId)];
    }

    public static String pickVehicle(String requestId) {
        return MECHANIC_VEHICLES[mechanicIndex(requestId)];
    }

    public static double pickRating(String requestId) {
        return MECHANIC_RATINGS[mechanicIndex(requestId)];
    }

    /**
     * Compute mechanic start latitude: user lat + offset (north).
     */
    public static double startLat(double userLat) {
        return userLat + START_OFFSET_DEG;
    }

    /**
     * Compute mechanic start longitude: user lng + offset (east).
     */
    public static double startLng(double userLng) {
        return userLng + START_OFFSET_DEG;
    }

    // ── Simulation ───────────────────────────────────────

    private TrackingResponse computeTracking(ServiceRequest req) {
        double userLat = req.getLatitude();
        double userLng = req.getLongitude();
        double mechStartLat = req.getMechanicStartLat();
        double mechStartLng = req.getMechanicStartLng();

        // Total distance from mechanic start to user (Haversine)
        double totalDistMetres = haversineMetres(mechStartLat, mechStartLng, userLat, userLng);

        // Elapsed time since assignment
        long elapsedSeconds = Duration.between(req.getAssignedAt(), LocalDateTime.now()).getSeconds();

        // Distance covered at constant speed
        double coveredMetres = elapsedSeconds * SPEED_MPS;

        // Progress fraction [0.0 .. 1.0]
        double progress = Math.min(coveredMetres / Math.max(totalDistMetres, 1.0), 1.0);

        // Current mechanic position (linear interpolation)
        double mechLat = mechStartLat + (userLat - mechStartLat) * progress;
        double mechLng = mechStartLng + (userLng - mechStartLng) * progress;

        // Remaining distance
        double remainingMetres = Math.max(totalDistMetres - coveredMetres, 0);

        // ETA in seconds
        long etaSeconds = (remainingMetres > 0)
                ? Math.round(remainingMetres / SPEED_MPS)
                : 0;

        // Determine tracking status
        TrackingStatus status;
        if (progress >= 1.0) {
            status = TrackingStatus.ARRIVED;
        } else if (elapsedSeconds > 30) {
            status = TrackingStatus.ON_THE_WAY;
        } else {
            status = TrackingStatus.ASSIGNED;
        }

        return TrackingResponse.builder()
                .requestId(req.getId())
                .trackingStatus(status)
                .vehicleType(req.getVehicleType())
                .vehicleNumber(req.getVehicleNumber())
                .problemDescription(req.getProblemDescription())
                .selectedIssue(req.getSelectedIssue())
                .additionalNotes(req.getAdditionalNotes())
                .address(req.getAddress())
                .eta(formatEta(etaSeconds))
                .distanceRemaining(formatDistance(remainingMetres))
                .etaSeconds(etaSeconds)
                .distanceMeters(remainingMetres)
                .userLat(userLat)
                .userLng(userLng)
                .mechanicLat(mechLat)
                .mechanicLng(mechLng)
                .mechanicName(req.getMechanicName())
                .mechanicPhone(req.getMechanicPhone())
                .mechanicVehicle(req.getMechanicVehicle())
                .mechanicRating(req.getMechanicRating())
                .build();
    }

    // ── Helpers ───────────────────────────────────────────

    /** Haversine formula: distance in metres between two lat/lng points. */
    static double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6_371_000; // Earth radius in metres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static String formatEta(long seconds) {
        if (seconds <= 0) return "Arrived";
        long mins = (seconds + 59) / 60; // ceil
        return mins + " min";
    }

    private static String formatDistance(double metres) {
        if (metres <= 0) return "0 m";
        if (metres < 1000) return Math.round(metres) + " m";
        return String.format("%.1f km", metres / 1000.0);
    }
}
