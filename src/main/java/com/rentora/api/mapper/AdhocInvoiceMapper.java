package com.rentora.api.mapper;

import com.rentora.api.model.dto.MonthlyInvoice.Response.UnitAdhocInvoice;
import com.rentora.api.model.entity.AdhocInvoice;
import org.springframework.stereotype.Component;

@Component
public class AdhocInvoiceMapper {
    public UnitAdhocInvoice toUnitAdhocInvoice(AdhocInvoice adhocInvoice) {
        return UnitAdhocInvoice.builder()
                .adhocId(adhocInvoice.getId())
                .adhocTitle(adhocInvoice.getTitle())
                .adhocNumber(adhocInvoice.getAdhocNumber())
                .amount(adhocInvoice.getFinalAmount())
                .build();
    }
}
