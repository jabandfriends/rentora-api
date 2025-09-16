package com.rentora.api.model.dto.Contract.Response;

import com.rentora.api.model.entity.Contract;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractSummaryDto {
    private String id;
    private String contractNumber;
    private String unitName;
    private String buildingName;
    private String apartmentName;
    private String tenantName;
    private String tenantEmail;
    private Contract.RentalType rentalType;
    private String startDate;
    private String endDate;
    private BigDecimal rentalPrice;
    private Contract.ContractStatus status;
    private String createdAt;

    // Additional summary info
    private Boolean isExpiringSoon; // within 30 days
    private Long daysUntilExpiry;
    private BigDecimal totalDeposit;
}