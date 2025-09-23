package com.rentora.api.specifications;

import org.springframework.data.jpa.domain.Specification;

import com.rentora.api.model.entity.Invoice;

public final class InvoiceSpecification {

    private InvoiceSpecification() {}

    public static Specification<Invoice> invoiceNumberContains(String invoiceNumber) {
        return (root, query, cd) -> {
            if (invoiceNumber == null || invoiceNumber.isBlank()) return null;
            String like = "%" + invoiceNumber.trim().toLowerCase() + "%";
            return cd.like(cd.lower(root.get("invoiceNumber")), like);
        };
    }

    public static Specification<Invoice> status(Invoice.PaymentStatus status) {
        return (root, query, cb) -> (status == null) ? null
                : cb.equal(root.get("paymentStatus"), status);
    }

}
