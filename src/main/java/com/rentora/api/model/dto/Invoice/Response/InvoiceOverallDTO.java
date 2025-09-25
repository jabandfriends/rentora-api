package com.rentora.api.model.dto.Invoice.Response;

import com.rentora.api.model.entity.Invoice;
import lombok.Data;

import java.util.List;

@Data
public class InvoiceOverallDTO {

    private long totalInvoice;
    private long paidInvoice;
    private long unpaidInvoice;
    private long partiallyPaidInvoice;
    private long overdueInvoice;
    private long cancelledInvoice;

}
