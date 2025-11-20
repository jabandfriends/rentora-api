package com.rentora.api.specifications;

import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class PaymentSpecification {
    public static Specification<Payment> hasPaymentStatus(Payment.PaymentStatus paymentStatus) {
        return (root, query, criteriaBuilder) -> paymentStatus == null ? null : criteriaBuilder.equal(root.get("paymentStatus"),paymentStatus);
    }

    public static Specification<Payment> hasVerificationStatus(Payment.VerificationStatus verificationStatus) {
        return (root, query, criteriaBuilder) ->
                verificationStatus == null ? null : criteriaBuilder.equal(root.get("verificationStatus"),verificationStatus);
    }

    public static Specification<Payment> hasUser(UUID userId) {
        return (root, query, criteriaBuilder) ->  userId == null ? null :
                criteriaBuilder.equal(root.get("invoice").get("contract").get("tenant").get("id"),userId);
    }

    public static Specification<Payment> hasApartment(UUID apartmentId) {
        return (root, query, criteriaBuilder) ->  apartmentId == null ? null : criteriaBuilder.equal(root.get("invoice").get("apartment").get("id"),apartmentId);
    }
    public static Specification<Payment> hasBuilding(String buildingName) {
        return (root, query, criteriaBuilder) -> buildingName == null ? null : criteriaBuilder.equal(root.get("invoice").get("unit").get("floor").get("building")
                .get("name"), buildingName);
    }
    public static Specification<Payment> matchGenerationDate(LocalDate generationDate) {
        return (root,query,criteriaBuilder)->
                generationDate == null ? null :
                        criteriaBuilder.equal(root.get("invoice").get("genMonth"), generationDate);
    }
}
