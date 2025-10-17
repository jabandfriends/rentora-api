package com.rentora.api.specifications;

import com.rentora.api.model.entity.Maintenance;
import com.rentora.api.model.entity.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class PaymentSpecification {
    public static Specification<Payment> hasPaymentStatus(Payment.PaymentStatus paymentStatus) {
        return (root, query, criteriaBuilder) -> paymentStatus == null ? null : criteriaBuilder.equal(root.get("paymentStatus"),paymentStatus);
    }

    public static Specification<Payment> hasApartment(UUID apartmentId) {
        return (root, query, criteriaBuilder) ->  apartmentId == null ? null : criteriaBuilder.equal(root.get("invoice").get("apartment").get("id"),apartmentId);
    }
    public static Specification<Payment> hasBuilding(String buildingName) {
        return (root, query, criteriaBuilder) -> buildingName == null ? null : criteriaBuilder.equal(root.get("invoice").get("unit").get("floor").get("building")
                .get("name"), buildingName);
    }
}
