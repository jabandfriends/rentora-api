package com.rentora.api.repository;

import com.rentora.api.model.entity.AdhocInvoice;

import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdhocInvoiceRepository extends JpaRepository<AdhocInvoice, UUID>, JpaSpecificationExecutor<AdhocInvoice> {
    Page<AdhocInvoice> findByApartmentId(UUID apartmentId, Pageable pageable);
    Page<AdhocInvoice> findByApartmentIdAndPaymentStatus(UUID apartmentId, AdhocInvoice.PaymentStatus status, Pageable pageable);

    List<AdhocInvoice> findByUnit(Unit unit);

    List<AdhocInvoice> findByUnitAndIncludeInMonthlyAndPaymentStatusAndStatus(Unit unit, Boolean includeInMonthly, AdhocInvoice.PaymentStatus paymentStatus, AdhocInvoice.InvoiceStatus status);
    List<AdhocInvoice> findByMonthlyInvoiceId(Invoice monthlyInvoiceId);
}


