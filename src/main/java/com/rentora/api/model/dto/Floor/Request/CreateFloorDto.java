package com.rentora.api.model.dto.Floor.Request;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.rentora.api.model.entity.Building;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateFloorDto {
    @NotNull(message = "Building id is required.")
    private UUID buildingId;

    @Min(value = 1,message = "Floor number is required or at least 1.")
    private Integer floorNumber;

    private String floorName;

    @Min(value = 1,message = "Total units is at least 1")
    private Integer totalUnits = 0;

}
