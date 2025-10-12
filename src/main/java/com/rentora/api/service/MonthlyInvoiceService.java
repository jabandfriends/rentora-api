package com.rentora.api.service;

import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.model.entity.*;
import com.rentora.api.repository.AdhocInvoiceRepository;
import com.rentora.api.repository.ContractRepository;
import com.rentora.api.repository.InvoiceRepository;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.specifications.UnitUtilitySpecification;
import com.rentora.api.specifications.UtilitySpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MonthlyInvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final AdhocInvoiceRepository adhocInvoiceRepository;
    private final ContractRepository contractRepository;
    private final UnitUtilityRepository unitUtilityRepository;

    public void createMonthlyInvoice(User currentAdmin, UUID unitId, Integer readingMonth, Integer readingYear, Integer paymentDueDays) {
        Invoice monthlyInvoiceResult = new Invoice();

        Contract activeContract = contractRepository.findActiveContractByUnitId(unitId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        //apartment id for this contract
        Apartment contractApartment = activeContract.getUnit().getFloor().getBuilding().getApartment();

        //invoice reading date for unitUtility
        LocalDate date = LocalDate.now().withDayOfMonth(1).withYear(readingYear).withMonth(readingMonth);


        Specification<UnitUtilities> currentWaterSpec = UnitUtilitySpecification.hasUnitId(activeContract.getUnit().getId())
                .and(UnitUtilitySpecification.hasUtilityName("water").and(UnitUtilitySpecification.hasUsageMonth(date)));
        UnitUtilities latestWaterMeter = unitUtilityRepository.findOne(currentWaterSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Current water meter reading not found"));

        Specification<UnitUtilities> currentElectricSpec = UnitUtilitySpecification.hasUnitId(activeContract.getUnit().getId())
                .and(UnitUtilitySpecification.hasUtilityName("electric").and(UnitUtilitySpecification.hasUsageMonth(date)));
        UnitUtilities latestElectricMeter = unitUtilityRepository.findOne(currentWaterSpec)
                .orElseThrow(() -> new ResourceNotFoundException("Current water meter reading not found"));


        BigDecimal waterUtilityPrice = BigDecimal.ZERO;
        BigDecimal electricUtilityPrice = BigDecimal.ZERO;


        //in case contract = monthly
        Contract.RentalType contractRentType = activeContract.getRentalType();

        //calculate utilityAmount
        //find utility and type
        Utility waterUtility = latestWaterMeter.getUtility();
        Utility.UtilityType waterUtilityType = waterUtility.getUtilityType();
        Utility electricUtility = latestElectricMeter.getUtility();
        Utility.UtilityType electricUtilityType = electricUtility.getUtilityType();

        if(waterUtilityType.equals(Utility.UtilityType.fixed)) {
            waterUtilityPrice = waterUtility.getFixedPrice();

        }else if(waterUtilityType.equals(Utility.UtilityType.meter)) {
            waterUtilityPrice = waterUtility.getUnitPrice().multiply(latestWaterMeter.getUsageAmount());

        }

        if(electricUtilityType.equals(Utility.UtilityType.fixed)) {
            electricUtilityPrice = electricUtility.getFixedPrice();
        }else if(electricUtilityType.equals(Utility.UtilityType.meter)) {
            electricUtilityPrice = electricUtility.getUnitPrice().multiply(latestElectricMeter.getUsageAmount());
        }
        BigDecimal utilityAmount = waterUtilityPrice.add(electricUtilityPrice);

        //----------adhoc invoice section ----------------
        List<AdhocInvoice> adhocInvoices = adhocInvoiceRepository.findByUnit(activeContract.getUnit());
        BigDecimal totalAdhocInvoiceAmount = BigDecimal.ZERO;
        for(AdhocInvoice adhocInvoice : adhocInvoices) {
            totalAdhocInvoiceAmount = totalAdhocInvoiceAmount.add(adhocInvoice.getFinalAmount());
        }

        BigDecimal totalAmount = BigDecimal.ZERO.add(utilityAmount).add(totalAdhocInvoiceAmount); //prepared for adhoc + service

        monthlyInvoiceResult.setApartment(contractApartment);
        monthlyInvoiceResult.setUnit(activeContract.getUnit());
        monthlyInvoiceResult.setContract(activeContract);
        monthlyInvoiceResult.setTenant(activeContract.getTenant());
        monthlyInvoiceResult.setFeesAmount(contractApartment.getLateFee());
        monthlyInvoiceResult.setDiscountAmount(BigDecimal.ZERO);
        monthlyInvoiceResult.setGeneratedByUser(currentAdmin);
        monthlyInvoiceResult.setPaymentDueDate(LocalDate.now().plusDays(paymentDueDays));
        if(contractRentType.equals(Contract.RentalType.monthly)){

            monthlyInvoiceResult.setBillStart(LocalDate.now());
            monthlyInvoiceResult.setBillEnd(LocalDate.now().plusDays(paymentDueDays));
            monthlyInvoiceResult.setGenMonth(LocalDate.now());
            monthlyInvoiceResult.setDueDate(LocalDate.now().plusDays(paymentDueDays));
            monthlyInvoiceResult.setRentAmount(activeContract.getRentalPrice());

            //calculate util
            monthlyInvoiceResult.setUtilAmount(utilityAmount);

            //calculate service
//            monthlyInvoiceResult.setServiceAmount();


            //adhoc invoice

            //add rent price
            totalAmount = totalAmount.add(activeContract.getRentalPrice());
            monthlyInvoiceResult.setTotalAmount(totalAmount);


        } else if (contractRentType.equals(Contract.RentalType.daily)) {
            monthlyInvoiceResult.setBillStart(activeContract.getStartDate());
            monthlyInvoiceResult.setBillEnd(activeContract.getEndDate());
            monthlyInvoiceResult.setGenMonth(LocalDate.now());
            monthlyInvoiceResult.setDueDate(LocalDate.now().plusDays(paymentDueDays));

            long daysBetween = ChronoUnit.DAYS.between(activeContract.getStartDate(), activeContract.getEndDate());
            BigDecimal rentalAmount =activeContract.getRentalPrice().multiply(BigDecimal.valueOf(daysBetween));
            monthlyInvoiceResult.setRentAmount(rentalAmount);

            //calculate service
//            monthlyInvoiceResult.setServiceAmount();

            //add rent price
            totalAmount =totalAmount.add(rentalAmount);
            monthlyInvoiceResult.setTotalAmount(totalAmount);

        } else if (contractRentType.equals(Contract.RentalType.yearly)){
            monthlyInvoiceResult.setBillStart(activeContract.getStartDate());
            monthlyInvoiceResult.setBillEnd(activeContract.getEndDate());
            monthlyInvoiceResult.setGenMonth(LocalDate.now());
            monthlyInvoiceResult.setDueDate(LocalDate.now().plusDays(paymentDueDays));

            //yearly because yearly is already paid rent amount
            monthlyInvoiceResult.setRentAmount(BigDecimal.ZERO);

            //calculate util
            monthlyInvoiceResult.setUtilAmount(utilityAmount);
            //calculate service
//            monthlyInvoiceResult.setServiceAmount(); should plus with util

            monthlyInvoiceResult.setTotalAmount(totalAmount);
        }
    }

}
