package com.rentora.api.model.dto.Report.Metadata;

import lombok.Data;

@Data
public class ReceiptReportMetaData {
    private long totalBill;
    private long receiptPaid;
    private long receiptUnpaid;
    private long receiptOverdue;
}
