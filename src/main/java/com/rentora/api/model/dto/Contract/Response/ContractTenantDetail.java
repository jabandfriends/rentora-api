package com.rentora.api.model.dto.Contract.Response;

import com.rentora.api.model.entity.Contract;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ContractTenantDetail {

    private String roomNumber;
    private String floorNumber;
    private String buildingName;

    private LocalDate startDate;
    private LocalDate endDate;
    private Long daysRemaining;
    private BigDecimal rentalPrice;
    private BigDecimal depositAmount;
    private Contract.RentalType rentalType;

    private List<LatestUtilityUsageResponseDto> utilityUsage;
    private List<ContractRoomServiceDto> roomServices;

}
