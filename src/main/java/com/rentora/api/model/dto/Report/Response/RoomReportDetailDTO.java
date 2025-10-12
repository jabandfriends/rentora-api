package com.rentora.api.model.dto.Report.Response;

import com.rentora.api.model.entity.Unit;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class RoomReportDetailDTO {
    private String id;
    private String roomName;
    private String tenantName;
    private String reservedName;
    private BigDecimal totalAmount;
    private String issueDate;
    private String dueDate;
    private String checkoutDate;
    private String status;

    // Current contract info
    private String currentContractId;
    private String currentTenantId;
    private String currentTenantName;
    private String currentTenantEmail;
    private BigDecimal currentRentalPrice;
}
