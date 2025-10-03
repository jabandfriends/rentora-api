package com.rentora.api.repository;

import com.rentora.api.model.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    @Query("SELECT i FROM Invoice i JOIN FETCH i.apartment a JOIN FETCH i.unit u JOIN FETCH i.contract c JOIN FETCH i.tenant usr WHERE i.id = :invoiceId")
    Optional<Invoice> findByInvoiceId(@Param("invoiceId") UUID invoiceId);




}
