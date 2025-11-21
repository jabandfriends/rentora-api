package com.rentora.api.ReceiptReportTest;

import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Unit;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.InvoiceRepository;
import com.rentora.api.service.ReceiptReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset; // Import ZoneOffset
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public abstract class ReceiptReportServiceBaseTest {

    @Mock
    protected AdhocInvoiceRepository adhocInvoiceRepository;
    @Mock
    protected InvoiceRepository invoiceRepository;

    @InjectMocks
    protected ReceiptReportService receiptReportService;

    // UUID Constants
    protected final UUID APARTMENT_ID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    protected final UUID ADHOC_ID_1 = UUID.fromString("20000000-0000-0000-0000-000000000001");
    protected final UUID INVOICE_ID_2 = UUID.fromString("30000000-0000-0000-0000-000000000002");
    protected final UUID UNIT_ID = UUID.fromString("40000000-0000-0000-0000-000000000000");

    // Dates and Pageable
    protected final LocalDate DUE_DATE = LocalDate.of(2025, 11, 10);
    protected final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 10, 1, 10, 0);
    protected final OffsetDateTime CREATED_AT_OFFSET = CREATED_AT.atOffset(ZoneOffset.UTC); // FIX: Safe OffsetDateTime
    protected final Pageable PAGEABLE = PageRequest.of(0, 10);

    // Mock Entities
    protected Apartment mockApartment;
    protected Unit mockUnit;
    protected AdhocInvoice mockAdhocPaid;
    protected Invoice mockInvoiceUnpaid;

    // Mock Pages
    protected Page<AdhocInvoice> mockAdhocPage;
    protected Page<Invoice> mockInvoicePage;


    @BeforeEach
    void setUpBase() {
        // Core Entities
        mockApartment = new Apartment();
        mockApartment.setId(APARTMENT_ID);

        mockUnit = new Unit();
        mockUnit.setId(UNIT_ID);

        // --- Adhoc Invoice Mock (PAID) ---
        mockAdhocPaid = new AdhocInvoice();
        mockAdhocPaid.setId(ADHOC_ID_1);
        mockAdhocPaid.setAdhocNumber("ADHOC-001");
        mockAdhocPaid.setApartment(mockApartment);
        mockAdhocPaid.setUnit(mockUnit);
        mockAdhocPaid.setFinalAmount(new BigDecimal("1500.00"));
        mockAdhocPaid.setPaidAmount(new BigDecimal("1500.00"));
        mockAdhocPaid.setInvoiceDate(LocalDate.of(2025, 10, 1));
        mockAdhocPaid.setDueDate(DUE_DATE);
        mockAdhocPaid.setPaymentStatus(AdhocInvoice.PaymentStatus.paid);
        mockAdhocPaid.setCreatedAt(CREATED_AT_OFFSET); // FIXED
        mockAdhocPaid.setUpdatedAt(CREATED_AT_OFFSET); // FIXED

        // --- Invoice Mock (UNPAID) ---
        mockInvoiceUnpaid = new Invoice();
        mockInvoiceUnpaid.setId(INVOICE_ID_2);
        mockInvoiceUnpaid.setInvoiceNumber("INV-2025-002");
        mockInvoiceUnpaid.setApartment(mockApartment);
        mockInvoiceUnpaid.setUnit(mockUnit);
        mockInvoiceUnpaid.setTotalAmount(new BigDecimal("4500.00"));
        mockInvoiceUnpaid.setPaidAmount(new BigDecimal("0.00"));
        mockInvoiceUnpaid.setBillStart(LocalDate.of(2025, 10, 1));
        mockInvoiceUnpaid.setDueDate(DUE_DATE);
        mockInvoiceUnpaid.setPaymentStatus(Invoice.PaymentStatus.unpaid);
        mockInvoiceUnpaid.setCreatedAt(CREATED_AT_OFFSET); // FIXED
        mockInvoiceUnpaid.setUpdatedAt(CREATED_AT_OFFSET); // FIXED


        // Mock Pages (for repository return values)
        mockAdhocPage = new PageImpl<>(List.of(mockAdhocPaid), PAGEABLE, 1);
        mockInvoicePage = new PageImpl<>(List.of(mockInvoiceUnpaid), PAGEABLE, 1);
    }
}