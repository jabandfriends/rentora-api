package com.rentora.api.model.dto.Apartment.Metadata;

import com.rentora.api.model.entity.ApartmentPayment;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpdateApartmentPaymentRequestDto {

    private ApartmentPayment.MethodType methodType;
    private String bankName;
    private String bankAccountNumber;
    private String accountHolderName;
    private String promptpayNumber;
    private String instructions;
    private Boolean isActive;
    private Integer displayOrder;
}
