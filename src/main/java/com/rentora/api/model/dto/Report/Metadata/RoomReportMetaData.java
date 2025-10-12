package com.rentora.api.model.dto.Report.Metadata;

import lombok.Data;

@Data
public class RoomReportMetaData {
    private long totalRooms;
    private long availableRooms;
    private long unavailableRooms;
}

