package com.rentora.api.model.dto.Apartment.Response;

import com.rentora.api.model.entity.Apartment;
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

    private String logoPresignedUrl;

    // Building count
    private Long buildingCount;
    private Long unitCount;
    private Long activeContractCount;
}