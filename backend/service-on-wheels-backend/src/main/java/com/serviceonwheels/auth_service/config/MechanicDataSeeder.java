package com.serviceonwheels.auth_service.config;

import com.serviceonwheels.auth_service.model.Mechanic;
import com.serviceonwheels.auth_service.model.MechanicStatus;
import com.serviceonwheels.auth_service.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the {@code mechanics} collection on startup if it is empty.
 * Uses the same mechanic pool that previously existed in TrackingService
 * but now stores them as real MongoDB documents.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MechanicDataSeeder implements ApplicationRunner {

    private final MechanicRepository mechanicRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (mechanicRepository.count() > 0) {
            log.info("Mechanics collection already populated ({} mechanics). Skipping seed.",
                    mechanicRepository.count());
            return;
        }

        List<Mechanic> mechanics = List.of(
                Mechanic.builder()
                        .name("Ravi Kumar")
                        .phone("+91 98765 43210")
                        .rating(4.8)
                        .vehicle("Bike KA01AB1234")
                        .status(MechanicStatus.AVAILABLE)
                        .currentLat(12.9716)
                        .currentLng(77.5946)
                        .build(),
                Mechanic.builder()
                        .name("Arjun Patel")
                        .phone("+91 98765 43211")
                        .rating(4.7)
                        .vehicle("Van KA02CD5678")
                        .status(MechanicStatus.AVAILABLE)
                        .currentLat(12.9816)
                        .currentLng(77.6046)
                        .build(),
                Mechanic.builder()
                        .name("Suresh R")
                        .phone("+91 98765 43212")
                        .rating(4.9)
                        .vehicle("Tow Truck KA03EF9012")
                        .status(MechanicStatus.AVAILABLE)
                        .currentLat(12.9616)
                        .currentLng(77.5846)
                        .build(),
                Mechanic.builder()
                        .name("Meena Devi")
                        .phone("+91 98765 43213")
                        .rating(4.6)
                        .vehicle("Bike KA04GH3456")
                        .status(MechanicStatus.AVAILABLE)
                        .currentLat(12.9916)
                        .currentLng(77.6146)
                        .build(),
                Mechanic.builder()
                        .name("Karthik S")
                        .phone("+91 98765 43214")
                        .rating(4.8)
                        .vehicle("Van KA05IJ7890")
                        .status(MechanicStatus.AVAILABLE)
                        .currentLat(12.9516)
                        .currentLng(77.5746)
                        .build()
        );

        mechanicRepository.saveAll(mechanics);
        log.info("Seeded {} mechanics into the database.", mechanics.size());
    }
}
