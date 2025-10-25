package com.rentora.api.service;

import com.rentora.api.model.dto.SupplyTransaction.Response.SupplyTransactionSummaryResponseDto;
import com.rentora.api.model.entity.SupplyTransaction;
import com.rentora.api.repository.MaintenanceSupplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class MaintenanceSupplyService {

    private MaintenanceSupplyRepository maintenanceSupplyRepository;


}
