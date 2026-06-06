package com.serviceonwheels.auth_service.repository;

import com.serviceonwheels.auth_service.model.Mechanic;
import com.serviceonwheels.auth_service.model.MechanicStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicRepository extends MongoRepository<Mechanic, String> {

    List<Mechanic> findByStatus(MechanicStatus status);

    Optional<Mechanic> findByActiveRequestId(String requestId);
}
