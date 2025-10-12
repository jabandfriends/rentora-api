package com.rentora.api.repository;

import com.rentora.api.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentServiceRepository extends JpaRepository<ServiceEntity, UUID> {

    List<ServiceEntity> findAllByApartmentId(UUID apartmentId);
}

