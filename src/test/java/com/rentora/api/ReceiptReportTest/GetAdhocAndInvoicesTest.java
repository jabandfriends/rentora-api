package com.rentora.api.ReceiptReportTest;

import com.rentora.api.model.dto.Report.Response.ReceiptReportDetailDTO;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.service.ReceiptReportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class GetAdhocAndInvoicesTest extends ReceiptReportServiceBaseTest {

    @Test
    @DisplayName("Test 1: GIVEN no status filter WHEN called THEN results from Adhoc and Invoice are combined correctly")
    void getAdhocAndInvoices_NoStatusFilter_CombinesAllResults() {
        // GIVEN
        // Setup repository mocks to return the base pages (1 Adhoc + 1 Invoice)
        when(adhocInvoiceRepository.findByApartmentId(eq(APARTMENT_ID), eq(PAGEABLE))).thenReturn(mockAdhocPage);
        when(invoiceRepository.findByApartment_Id(eq(APARTMENT_ID), eq(PAGEABLE))).thenReturn(mockInvoicePage);

        // WHEN
        // Call service with status = null (No filter)
        List<ReceiptReportDetailDTO> result = receiptReportService.getAdhocAndInvoices(APARTMENT_ID, null, PAGEABLE);

        // THEN
        assertNotNull(result);
        assertEquals(2, result.size(), "Result size should be 1 Adhoc + 1 Invoice.");

        // Verify Adhoc DTO
        ReceiptReportDetailDTO adhocDto = result.stream()
                .filter(dto -> dto.getId().equals(ADHOC_ID_1)).findFirst().orElseThrow();
        assertEquals("ADHOC-001", adhocDto.getAdhocNumber());
        assertEquals(new BigDecimal("1500.00"), adhocDto.getFinalAmount());
        assertEquals(AdhocInvoice.PaymentStatus.paid, adhocDto.getPaymentStatus());

        // Verify Invoice DTO
        ReceiptReportDetailDTO invoiceDto = result.stream()
                .filter(dto -> dto.getId().equals(INVOICE_ID_2)).findFirst().orElseThrow();
        assertEquals("INV-2025-002", invoiceDto.getAdhocNumber(), "Invoice number should map to adhocNumber field.");
        assertEquals(new BigDecimal("4500.00"), invoiceDto.getFinalAmount());
        assertEquals(AdhocInvoice.PaymentStatus.unpaid, invoiceDto.getPaymentStatus(), "Invoice status should be mapped to Adhoc status enum.");

        // Verify correct repository methods were called
        verify(adhocInvoiceRepository, times(1)).findByApartmentId(eq(APARTMENT_ID), eq(PAGEABLE));
        verify(invoiceRepository, times(1)).findByApartment_Id(eq(APARTMENT_ID), eq(PAGEABLE));
        verify(adhocInvoiceRepository, never()).findByApartmentIdAndPaymentStatus(any(UUID.class), any(), any());
    }

    @Test
    @DisplayName("Test 2: GIVEN status filter (PAID) WHEN called THEN only matched PAID results are returned")
    void getAdhocAndInvoices_WithStatusFilter_ReturnsOnlyMatchedStatus() {
        // GIVEN
        AdhocInvoice.PaymentStatus statusFilter = AdhocInvoice.PaymentStatus.paid;

        // Mock repositories to return results filtered by PAID status
        // Adhoc returns mockAdhocPage (PAID)
        when(adhocInvoiceRepository.findByApartmentIdAndPaymentStatus(eq(APARTMENT_ID), eq(statusFilter), eq(PAGEABLE)))
                .thenReturn(mockAdhocPage);

        // Invoice returns an empty page, as mockInvoiceUnpaid is UNPAID
        when(invoiceRepository.findByApartment_IdAndPaymentStatus(eq(APARTMENT_ID), eq(Invoice.PaymentStatus.paid), eq(PAGEABLE)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PAGEABLE, 0));

        // WHEN
        List<ReceiptReportDetailDTO> result = receiptReportService.getAdhocAndInvoices(APARTMENT_ID, statusFilter, PAGEABLE);

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size(), "Result size should be 1 (Adhoc PAID) because Invoice is UNPAID.");
        assertEquals(ADHOC_ID_1, result.getFirst().getId());

        // Verify correct repository methods were called
        verify(adhocInvoiceRepository, times(1)).findByApartmentIdAndPaymentStatus(eq(APARTMENT_ID), eq(statusFilter), eq(PAGEABLE));
        verify(invoiceRepository, times(1)).findByApartment_IdAndPaymentStatus(eq(APARTMENT_ID), eq(Invoice.PaymentStatus.paid), eq(PAGEABLE));
        verify(adhocInvoiceRepository, never()).findByApartmentId(any(UUID.class), any());
    }

    @Test
    @DisplayName("Test 3: GIVEN empty pages from both repositories WHEN called THEN an empty list is returned")
    void getAdhocAndInvoices_EmptyResults_ReturnsEmptyList() {
        // GIVEN
        // Setup empty pages for both
        Page<AdhocInvoice> mockEmptyAdhoc = new PageImpl<>(Collections.emptyList(), PAGEABLE, 0);
        Page<Invoice> mockEmptyInvoice = new PageImpl<>(Collections.emptyList(), PAGEABLE, 0);

        when(adhocInvoiceRepository.findByApartmentId(eq(APARTMENT_ID), eq(PAGEABLE))).thenReturn(mockEmptyAdhoc);
        when(invoiceRepository.findByApartment_Id(eq(APARTMENT_ID), eq(PAGEABLE))).thenReturn(mockEmptyInvoice);

        // WHEN
        List<ReceiptReportDetailDTO> result = receiptReportService.getAdhocAndInvoices(APARTMENT_ID, null, PAGEABLE);

        // THEN
        assertNotNull(result);
        assertTrue(result.isEmpty(), "The result list should be empty.");

        // Verify correct repository methods were called
        verify(adhocInvoiceRepository, times(1)).findByApartmentId(eq(APARTMENT_ID), eq(PAGEABLE));
        verify(invoiceRepository, times(1)).findByApartment_Id(eq(APARTMENT_ID), eq(PAGEABLE));
    }
}
