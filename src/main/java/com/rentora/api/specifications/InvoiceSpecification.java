package com.rentora.api.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.rentora.api.model.entity.Invoice;

import java.util.UUID;

public final class InvoiceSpecification {

    private InvoiceSpecification() {}

    public static Specification<Invoice> hasInvoiceNumber(String invoiceNumber) {
        return (root, query, criteriaBuilder) -> {
            if (invoiceNumber == null || invoiceNumber.isBlank()) return null;
            String like = "%" + invoiceNumber.trim().toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("invoiceNumber")), like);
        };
    }

    public static Specification<Invoice> hasStatus(Invoice.PaymentStatus status) {
        return (root, query, criteriaBuilder) -> (status == null) ? null
                : criteriaBuilder.equal(root.get("paymentStatus"), status);
    }

}
