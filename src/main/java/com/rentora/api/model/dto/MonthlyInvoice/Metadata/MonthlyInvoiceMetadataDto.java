package com.rentora.api.model.dto.MonthlyInvoice.Metadata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyInvoiceMetadataDto {
    long totalMonthlyInvoices;
    long totalUnpaidMonthlyInvoices;
    long totalPaidMonthlyInvoices;
    long totalOverdueMonthlyInvoice;
}


