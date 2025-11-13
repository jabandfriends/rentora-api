package com.rentora.api.model.dto.Apartment.Metadata;

import com.rentora.api.model.entity.ApartmentPayment;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateApartmentPaymentRequestDto {
    private String promptPayFilename;
    private String bankName;
    private String bankAccountNumber;
    private String accountHolderName;
    private String promptpayNumber;
    private String instructions;
}
