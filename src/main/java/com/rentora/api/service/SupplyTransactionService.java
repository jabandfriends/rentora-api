package com.rentora.api.service;

import com.rentora.api.mapper.SupplyTransactionMapper;
import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.SupplyTransaction;
import com.rentora.api.repository.SupplyTransactionRepository;
import com.rentora.api.specifications.SupplyTransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SupplyTransactionService {
    private final SupplyTransactionMapper supplyTransactionMapper;

    private final SupplyTransactionRepository supplyTransactionRepository;

    public Page<SupplyTransactionSummaryResponseDto> getSupplyTransactions(UUID apartmentId, String supplyName,
                                                                           SupplyTransaction.SupplyTransactionType category, Pageable pageable) {
        Specification<SupplyTransaction> supplyTransactionSpecification = SupplyTransactionSpecification.hasApartmentId(apartmentId)
                .and(SupplyTransactionSpecification.hasSupplyName(supplyName).and(SupplyTransactionSpecification.hasCategory(category)));

        Page<SupplyTransaction> supplyTransactions = supplyTransactionRepository.findAll(supplyTransactionSpecification,pageable);

        return supplyTransactions.map(supplyTransactionMapper::supplyTransactionSummaryResponseDto);
    }
}
