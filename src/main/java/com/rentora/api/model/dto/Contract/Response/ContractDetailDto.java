package com.rentora.api.model.dto.Contract.Response;

import com.rentora.api.model.entity.Contract;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractDetailDto {
    private String id;
    private String contractNumber;
    private String unitId;
    private String unitName;
    private String buildingName;
    private String apartmentName;
    private String tenantId;
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


    // Additional detail info
    private Boolean isActive;
    private Boolean isExpiringSoon;
    private Long daysUntilExpiry;
    private BigDecimal totalPaidDeposit;
    private Integer contractDurationDays;
}