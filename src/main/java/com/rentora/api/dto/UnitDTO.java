package com.rentora.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnitDTO {
    private Long id;        // optional for update
    private String name;
    private String status;
    private Integer floor;
}
