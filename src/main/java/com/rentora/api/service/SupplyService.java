package com.rentora.api.service;

import com.rentora.api.exception.BadRequestException;
import com.rentora.api.mapper.SupplyMapper;
import com.rentora.api.model.dto.Supply.Request.CreateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Request.UpdateSupplyRequestDto;
import com.rentora.api.model.dto.Supply.Response.SupplyMetaDataDto;
import com.rentora.api.model.dto.Supply.Response.SupplySummaryResponseDto;
import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Supply;
import com.rentora.api.repository.ApartmentRepository;
import com.rentora.api.repository.SupplyRepository;
import com.rentora.api.specifications.SupplySpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SupplyService {
    private final SupplyMapper supplyMapper;

    private final SupplyTransactionService supplyTransactionService;

    private final SupplyRepository supplyRepository;
    private final ApartmentRepository apartmentRepository;

    //get all supply filter by apartment , supply name , supply category
    public Page<SupplySummaryResponseDto> getAllSupplies(UUID apartmentId,String supplyName,
                                                         Supply.SupplyCategory supplyCategory, Pageable pageable) {

        Specification<Supply> supplySpecification = SupplySpecification.hasApartmentId(apartmentId).and(
                SupplySpecification.hasName(supplyName).and(SupplySpecification.hasCategory(supplyCategory))
                        .and(SupplySpecification.hasNotDelete())
        );
        Page<Supply> supplies = supplyRepository.findAll(supplySpecification,pageable);

        return supplies.map(supplyMapper::toSupplySummaryResponseDto);
    }

    //get metaData
    public SupplyMetaDataDto getSupplyMetadata(UUID apartmentId) {
        Apartment  apartment = apartmentRepository.findById(apartmentId).orElseThrow(() -> new BadRequestException("apartment not found"));

        long totalSupplies = supplyRepository.countByApartmentAndIsDeleted(apartment,false);
        long totalLowStockSupplies = supplyRepository.countLowStockByApartment(apartment,false);
        BigDecimal totalCostSupplies = supplyRepository.totalCostSuppliesByApartment(apartment,false);

        return supplyMapper.toSupplyMetaDataDto(totalSupplies,totalLowStockSupplies,totalCostSupplies);
    }


    //create supply
    public void createSupply(UUID apartmentId,CreateSupplyRequestDto request) {
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow(() -> new BadRequestException("Apartment not found"));

        Supply supply = supplyMapper.toCreateSupply(apartment, request);

        supplyRepository.save(supply);
    }

    //delete supply
    public void deleteSupplies(UUID supplyId) {
        if (supplyId == null) throw new BadRequestException("Please provide the supply id to delete supply");

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(() -> new BadRequestException("Supply not found"));
        supply.setIsDeleted(true);

        supplyRepository.save(supply);
    }

    //update supply
    public void updateSupplies(UUID supplyId,UUID userId, UpdateSupplyRequestDto request) {
        if (supplyId == null) throw new BadRequestException("Please provide the supply id to update supply");

        Supply supply = supplyRepository.findById(supplyId).orElseThrow(() -> new BadRequestException("Supply not found"));
        supplyTransactionService.createSupplyUpdateTransaction(supply,request,userId);
        supplyMapper.toUpdateSupply(supply, request);

        supplyRepository.save(supply);
    }


}
