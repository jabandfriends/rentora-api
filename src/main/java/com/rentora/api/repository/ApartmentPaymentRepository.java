package com.rentora.api.repository;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApartmentPaymentRepository extends JpaRepository<ApartmentPayment, UUID> {
    List<ApartmentPayment> findByApartment(Apartment apartment);

    Optional<ApartmentPayment> findByApartmentAndIsActive(Apartment apartment, Boolean isActive);

    @Modifying
    @Query("UPDATE ApartmentPayment p SET p.isActive = false WHERE p.apartment.id = :apartmentId AND p.id <> :currentPaymentId")
    void deactivateOtherPayments(@Param("apartmentId") UUID apartmentId, @Param("currentPaymentId") UUID currentPaymentId);
}
