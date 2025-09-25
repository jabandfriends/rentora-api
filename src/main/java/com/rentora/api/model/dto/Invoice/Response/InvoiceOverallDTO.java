package com.rentora.api.model.dto.Invoice.Response;

import lombok.Data;

import java.util.List;

@Data
public class InvoiceOverallDTO {

    private List<InvoiceSummaryDTO> overallDTO;
    private long totalInvoice;
    private long paidInvoice;
    private long unpaidInvoice;
    private long overdueInvoice;

}
