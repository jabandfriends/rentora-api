package com.rentora.api.model.dto.Unit.Metadata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnitMetadataDto {
    private long totalUnits;
    private long totalUnitsAvailable;
    private long totalUnitsMaintenance;
    private  long totalUnitsOccupied;

}
