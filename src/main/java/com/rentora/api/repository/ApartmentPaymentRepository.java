package com.rentora.api.repository;

import com.rentora.api.model.entity.ApartmentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApartmentPaymentRepository extends JpaRepository<ApartmentPayment, UUID> {
}
