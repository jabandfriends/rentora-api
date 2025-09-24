package com.rentora.api.service;

import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import com.rentora.api.repository.UnitUtilityRepository;
import com.rentora.api.repository.UtilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final UnitUtilityRepository unitUtilityRepository;

    //unit service

    public void getUnitsUtility(UUID unitId, Pageable pageable){

        Page<UnitUtilities> unitUtilities = unitUtilityRepository.findByUnitId(unitId,pageable);

        List<UnitUtilities> utilities = unitUtilities.getContent();

        //category , meter_end , meter_start
        UnitUtilities water = utilities.stream().filter(utilityItem-> utilityItem.getUtility().getUtilityName().equals("water")).toList().getFirst();
        //category ,meter_end , meter_start
        UnitUtilities electric = utilities.stream().filter(utilityItem-> utilityItem.getUtility().getUtilityName().equals("electric")).toList().getFirst();

        BigDecimal waterUsage;
        BigDecimal electricUsage;


        Utility.UtilityType waterType = water.getUtility().getUtilityType();
        Utility.UtilityType electricType =  electric.getUtility().getUtilityType();

        //meter
        if(waterType.equals(Utility.UtilityType.meter)){
            waterUsage = water.getMeterEnd().subtract(water.getMeterStart());
        }else{

        }
        if(electricType.equals(Utility.UtilityType.meter)){
            electricUsage = electric.getMeterEnd().subtract(electric.getMeterStart());
        }




    }


}
