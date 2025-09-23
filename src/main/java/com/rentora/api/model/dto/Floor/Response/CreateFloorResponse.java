package com.rentora.api.model.dto.Floor.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateFloorResponse {
    private UUID id; //floor
}
