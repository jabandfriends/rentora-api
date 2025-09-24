package com.rentora.api.service;

import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.repository.UtilityRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReportService {
    private final UnitUtilityRepository unitUtilityRepository;

    public ReportService(UnitUtilityRepository unitUtilityRepository, UtilityRepository utilityRepository) {
        this.unitUtilityRepository = unitUtilityRepository;

    }


}
