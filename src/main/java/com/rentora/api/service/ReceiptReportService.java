package com.rentora.api.service;

import com.rentora.api.model.dto.Report.Metadata.ReceiptReportMetaData;
import com.rentora.api.model.dto.Report.Response.ReceiptReportDetailDTO;
import com.rentora.api.model.entity.AdhocInvoice;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReceiptReportService {

    private final AdhocInvoiceRepository adhocInvoiceRepository;
    private final InvoiceRepository invoiceRepository;

    public List<ReceiptReportDetailDTO> getAdhocAndInvoices(
            UUID apartmentId,
            AdhocInvoice.PaymentStatus status,
            Pageable pageable) {

        Page<AdhocInvoice> adhocInvoices;
        Page<Invoice> invoices;

        if (status != null) {
            adhocInvoices = adhocInvoiceRepository
                    .findByApartmentIdAndPaymentStatus(apartmentId, status, pageable);
            invoices = invoiceRepository
                    .findByApartment_IdAndPaymentStatus(apartmentId, Invoice.PaymentStatus.valueOf(status.name()), pageable);
        } else {
            adhocInvoices = adhocInvoiceRepository.findByApartmentId(apartmentId, pageable);
            invoices = invoiceRepository.findByApartment_Id(apartmentId, pageable);
        }

        List<ReceiptReportDetailDTO> result = new ArrayList<>();
        result.addAll(adhocInvoices.map(this::toDtoFromAdhoc).toList());
        result.addAll(invoices.map(this::toDtoFromInvoice).toList());


        System.out.println(">>> adhoc = " + adhocInvoices.getTotalElements());
        System.out.println(">>> invoice = " + invoices.getTotalElements());

        return result;
    }


    private ReceiptReportDetailDTO toDtoFromAdhoc(AdhocInvoice entity) {
        ReceiptReportDetailDTO dto = new ReceiptReportDetailDTO();
        dto.setId(entity.getId().toString());
        dto.setAdhocNumber(entity.getAdhocNumber());
        dto.setApartmentId(entity.getApartmentId().toString());
        dto.setUnitId(entity.getUnit() != null ? entity.getUnit().getId().toString() : null);
        dto.setFinalAmount(entity.getFinalAmount());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setInvoiceDate(entity.getInvoiceDate() != null ? entity.getInvoiceDate().toString() : null);
        dto.setDueDate(entity.getDueDate() != null ? entity.getDueDate().toString() : null);
        dto.setPaymentStatus(entity.getPaymentStatus());
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return dto;
    }

    private ReceiptReportDetailDTO toDtoFromInvoice(Invoice entity) {
        ReceiptReportDetailDTO dto = new ReceiptReportDetailDTO();
        dto.setId(entity.getId().toString());
        dto.setAdhocNumber(entity.getInvoiceNumber()); // ใช้ช่องเดียวกันเก็บเลข invoice
        dto.setApartmentId(entity.getApartment().getId().toString());
        dto.setUnitId(entity.getUnit() != null ? entity.getUnit().getId().toString() : null);
        dto.setFinalAmount(entity.getTotalAmount());
        dto.setPaidAmount(entity.getPaidAmount());
        dto.setInvoiceDate(entity.getBillStart() != null ? entity.getBillStart().toString() : null);
        dto.setDueDate(entity.getDueDate() != null ? entity.getDueDate().toString() : null);
        dto.setPaymentStatus(AdhocInvoice.PaymentStatus.valueOf(entity.getPaymentStatus().name()));
        dto.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt().toString() : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt().toString() : null);
        return dto;
    }

    public ReceiptReportMetaData getReceiptReportMetadata(UUID apartmentId) {
        List<AdhocInvoice> adhocInvoices = adhocInvoiceRepository.findByApartmentId(apartmentId, Pageable.unpaged()).getContent();
        List<Invoice> invoices = invoiceRepository.findByApartment_Id(apartmentId, Pageable.unpaged()).getContent();

        long totalBill = adhocInvoices.size() + invoices.size();
        long paid = adhocInvoices.stream().filter(inv -> inv.getPaymentStatus() == AdhocInvoice.PaymentStatus.paid).count()
                + invoices.stream().filter(inv -> inv.getPaymentStatus() == Invoice.PaymentStatus.paid).count();
        long unpaid = adhocInvoices.stream().filter(inv -> inv.getPaymentStatus() == AdhocInvoice.PaymentStatus.unpaid).count()
                + invoices.stream().filter(inv -> inv.getPaymentStatus() == Invoice.PaymentStatus.unpaid).count();
        long overdue = adhocInvoices.stream().filter(inv ->
                inv.getPaymentStatus() == AdhocInvoice.PaymentStatus.unpaid &&
                        inv.getDueDate() != null &&
                        inv.getDueDate().isBefore(LocalDate.now())
        ).count()
                + invoices.stream().filter(inv ->
                inv.getPaymentStatus() == Invoice.PaymentStatus.unpaid &&
                        inv.getDueDate() != null &&
                        inv.getDueDate().isBefore(LocalDate.now())
        ).count();

        ReceiptReportMetaData metadata = new ReceiptReportMetaData();
        metadata.setTotalBill(totalBill);
        metadata.setReceiptPaid(paid);
        metadata.setReceiptUnpaid(unpaid);
        metadata.setReceiptOverdue(overdue);

        return metadata;
    }
}





