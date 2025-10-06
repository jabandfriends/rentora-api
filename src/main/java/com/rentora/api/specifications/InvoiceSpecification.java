package com.rentora.api.specifications;

import com.rentora.api.model.entity.AdhocInvoice;
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

    public static Specification<Invoice> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId);
    }

    public static Specification<Invoice> hasOverdueStatus() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paymentStatus"), AdhocInvoice.PaymentStatus.overdue);
    }

    public static Specification<AdhocInvoice> hasInvoiceNumberForAdhoc(String adhocInvoiceNumber) {
        return (root, query, criteriaBuilder) -> {
            if (adhocInvoiceNumber == null || adhocInvoiceNumber.isBlank()) return null;
            String like = "%" + adhocInvoiceNumber.trim().toLowerCase() + "%";
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("adhocNumber")), like);
        };
    }

    public static Specification<AdhocInvoice> hasStatusForAdhoc(AdhocInvoice.PaymentStatus status) {
        return (root, query, criteriaBuilder) -> (status == null) ? null
                : criteriaBuilder.equal(root.get("paymentStatus"), status);
    }

    public static Specification<AdhocInvoice> hasOverdueStatusForAdhoc() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("paymentStatus"), AdhocInvoice.PaymentStatus.overdue);
    }

    public static Specification<AdhocInvoice> hasApartmentIdForAdhoc(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId);
    }
    public static Specification<AdhocInvoice> hasAdhocId(UUID id) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
    }

}
