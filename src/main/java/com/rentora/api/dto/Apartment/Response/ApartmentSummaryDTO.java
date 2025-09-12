package com.rentora.api.dto.Apartment.Response;

import com.rentora.api.entity.Apartment;
import lombok.Data;

@Data
public class ApartmentSummaryDTO {
    private String id;
    private String name;
    private String logoUrl;
    private String phoneNumber;
    private String address;
    private String city;
    private String state;
    private Apartment.ApartmentStatus status;
    private String createdAt;
    private String updatedAt;

    // Building count
    private Long buildingCount;
    private Long unitCount;
    private Long activeContractCount;
}