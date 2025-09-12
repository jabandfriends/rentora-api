package com.rentora.api.dto.Apartment.Response;

import com.rentora.api.entity.Apartment;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApartmentDetailDTO {
    private String id;
    private String name;
    private String logoUrl;
    private String phoneNumber;
    private String taxId;
    private Integer paymentDueDay;
    private BigDecimal lateFee;
    private Apartment.LateFeeType lateFeeType;
    private Integer gracePeriodDays;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String timezone;
    private String currency;
    private Apartment.ApartmentStatus status;
    private String createdAt;
    private String updatedAt;

    // Statistics
    private Long buildingCount;
    private Long unitCount;
    private Long activeContractCount;
    private Long totalTenants;
}