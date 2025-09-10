package com.rentora.api.repository;

import com.rentora.api.entity.ApartmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApartmentsRepository extends JpaRepository<ApartmentsEntity,Long> {
    Page<ApartmentsEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
