package com.serviceonwheels.auth_service.repository;

import com.serviceonwheels.auth_service.model.ServiceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {

    List<ServiceRequest> findByUserIdOrderByCreatedAtDesc(String userId);
}
