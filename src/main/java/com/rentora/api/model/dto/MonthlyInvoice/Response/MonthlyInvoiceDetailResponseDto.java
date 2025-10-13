package com.rentora.api.model.dto.MonthlyInvoice.Response;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Invoice;
import com.rentora.api.model.entity.Utility;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class MonthlyInvoiceDetailResponseDto {
    private UUID invoiceId;
    private String unitName;
    private String buildingName;
    private String tenantName;
    private BigDecimal totalAmount; //rent + utility + service + else
    private Invoice.PaymentStatus paymentStatus;

    //new
    private String invoiceNumber;
    private String tenantPhone;
    private String tenantEmail;
    private BigDecimal waterAmount;
    private BigDecimal electricAmount;
    private BigDecimal rentAmount;
    private BigDecimal contractRentAmount;
    private String floorName;
    private String contractNumber;
    private LocalDate dueDate;
    private Integer invoiceMonth;
    private Integer invoiceYear;
    private Contract.RentalType rentalType;


    //water usage
    private BigDecimal waterMeterStart;
    private BigDecimal waterMeterEnd;
    private BigDecimal totalWaterUsageUnit;
    private BigDecimal waterPricePerUnit;
    private Utility.UtilityType waterPriceRateType;
    private BigDecimal waterTotalCost;

    //electric
    private BigDecimal electricMeterStart;
    private BigDecimal electricMeterEnd;
    private BigDecimal totalElectricUsageUnit;
    private BigDecimal electricPricePerUnit;
    private Utility.UtilityType electricPriceRateType;
    private BigDecimal electricTotalCost;

}
