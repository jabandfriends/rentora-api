package com.rentora.api.model.dto.Contract.Response;

import com.rentora.api.model.entity.Contract;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ContractDetailDto {
    private UUID contractId;
    private String contractNumber;
    private String unitName;
    private String buildingName;
    private String apartmentName;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;
    private String guarantorName;
    private String guarantorPhone;
    private String guarantorIdNumber;
    private Contract.RentalType rentalType;
    private String startDate;
    private String endDate;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private Integer advancePaymentMonths;
    private BigDecimal lateFeeAmount;
    private Boolean utilitiesIncluded;
    private String termsAndConditions;
    private String specialConditions;
    private Contract.ContractStatus status;
    private Boolean autoRenewal;
    private Integer renewalNoticeDays;
    private String terminationDate;
    private String terminationReason;
    private String terminatedByUserName;
    private String documentUrl;
    private String signedAt;
    private String createdByUserName;
    private String createdAt;
    private String updatedAt;

    private BigDecimal waterMeterStart;
    private BigDecimal electricMeterStart;


    // Additional detail info
    private Long daysUntilExpiry;
    private Integer contractDurationDays;
}