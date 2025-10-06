package com.rentora.api.repository;

import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<AdhocInvoice, UUID>, JpaSpecificationExecutor<AdhocInvoice> {

}



