package com.rentora.api.model.dto.Invoice.Metadata;

import lombok.Data;

@Data
public class InvoiceOverallDTO {

    private long totalInvoice;
    private long paidInvoice;
    private long unpaidInvoice;
    private long partiallyPaidInvoice;
    private long overdueInvoice;
    private long cancelledInvoice;

}
