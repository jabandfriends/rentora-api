package com.rentora.api.model.dto.Invoice.Metadata;
import lombok.Data;

@Data
public class AdhocInvoiceOverallDTO {

    private long totalInvoice;
    private long paidInvoice;
    private long unpaidInvoice;
    private long overdueInvoice;

}
