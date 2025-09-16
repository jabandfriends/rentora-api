package com.rentora.api.repository;

import com.rentora.api.model.entity.ApartmentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApartmentUserRepository extends JpaRepository<ApartmentUser, UUID> {
}

