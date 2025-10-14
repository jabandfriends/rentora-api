package com.rentora.api.model.dto.Invoice.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ExecuteAdhocInvoiceResponse {
    private UUID adhocInvoiceId;
}
