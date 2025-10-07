package com.rentora.api.repository;

import com.rentora.api.model.entity.AdhocInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdhocInvoiceRepository extends JpaRepository<AdhocInvoice, UUID>, JpaSpecificationExecutor<AdhocInvoice> {

}



