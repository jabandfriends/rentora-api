package com.rentora.api.repository;

import com.rentora.api.model.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
}

