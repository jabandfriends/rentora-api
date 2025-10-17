package com.rentora.api.specifications;

import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class MonthlyInvoiceSpecification {
    public static Specification<Invoice> hasApartmentId(UUID apartmentId) {
        return (root,query,criteriaBuilder)->
                apartmentId == null ? null : criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId);
    }

    public static Specification<Invoice> hasPaymentStatus(Invoice.PaymentStatus status) {
        return (root,query,criteriaBuilder)->
                status == null ? null : criteriaBuilder.equal(root.get("paymentStatus"), status);
    }

    public static Specification<Invoice> hasUnitName(String unitName) {
        return (root,query,criteriaBuilder)->
                unitName == null || unitName.isEmpty() ? null :
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("unit").get("unitName")), "%" + unitName.toLowerCase() + "%");
    }

    public static Specification<Invoice> hasBuildingName(String buildingName) {
        return (root,query,criteriaBuilder)-> buildingName == null || buildingName.isEmpty() ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("unit").get("floor").get("building").get("name")), "%" + buildingName.toLowerCase() + "%");
    }

    public static Specification<Invoice> matchGenerationDate(LocalDate generationDate) {
        return (root,query,criteriaBuilder)->
                generationDate == null ? null :
                        criteriaBuilder.equal(root.get("genMonth"), generationDate);
    }
}
