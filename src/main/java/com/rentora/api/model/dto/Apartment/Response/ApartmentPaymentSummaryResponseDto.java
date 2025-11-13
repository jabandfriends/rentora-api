package com.rentora.api.model.dto.Apartment.Response;

import com.rentora.api.model.entity.ApartmentPayment;
import lombok.Builder;
import lombok.Data;

import java.net.URL;
import java.util.UUID;

@Data
@Builder
public class ApartmentPaymentSummaryResponseDto {
    private final UUID apartmentPaymentId;
    private final ApartmentPayment.MethodType methodType;
    private final String bankName;
    private final String bankAccountNumber;
    private final String accountHolderName;
    private final String promptpayNumber;
    private final URL promptpayURL;
    private final Boolean isActive;
    private final Integer displayOrder;
    private final String createByUserName;
}
