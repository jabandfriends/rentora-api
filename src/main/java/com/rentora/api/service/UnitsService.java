package com.rentora.api.service;

import com.rentora.api.entity.UnitEntity;
import com.rentora.api.repository.UnitsRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class UnitsService {

    private final UnitsRepository unitsRepository;

    public UnitsService(UnitsRepository unitsRepository) {
        this.unitsRepository = unitsRepository;
    }

    public List<UnitEntity> getUnitsByApartment(Long apartmentId) {
        return unitsRepository.findByApartmentId(apartmentId);
    }
}
