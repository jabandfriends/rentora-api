package com.rentora.api.repository;

import com.rentora.api.model.entity.ApartmentService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApartmentServiceRepository extends JpaRepository<ApartmentService, UUID>, JpaSpecificationExecutor<ApartmentService> {

    List<ApartmentService> findAllByApartmentId(UUID apartmentId);
    Optional<ApartmentService> findByServiceName(String serviceName);
}

