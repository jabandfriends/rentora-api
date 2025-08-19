package com.rentora.api.service;

import com.rentora.api.dto.UnitDTO;
import com.rentora.api.entity.ApartmentsEntity;
import com.rentora.api.entity.UnitEntity;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.ApartmentsRepository;
import com.rentora.api.repository.UnitsRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
@AllArgsConstructor
public class UnitsService {

    private final UnitsRepository unitsRepository;
    private final ApartmentsRepository apartmentsRepository;

    public List<UnitEntity> getUnitsByApartment(Long apartmentId) {
        List<UnitEntity> data = unitsRepository.findByApartmentId(apartmentId);
        if(data.isEmpty()){
            throw new ResourceNotFoundException("no units found");
        }
        return data;
    }

    // ---- Create multiple units ----
    @Transactional
    public List<UnitEntity> createUnits(Long apartmentId, List<UnitDTO> unitsDto) {
        ApartmentsEntity apartment = apartmentsRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));

        return unitsDto.stream().map(dto ->
                unitsRepository.save(UnitEntity.builder()
                        .name(dto.getName())
                        .status(dto.getStatus())
                        .floor(dto.getFloor())
                        .createdAt(LocalDateTime.now())
                        .apartment(apartment)
                        .build())
        ).toList();
    }

    // ---- Update multiple units ----
    @Transactional
    public List<UnitEntity> updateUnits(List<UnitDTO> unitsDto) {
        return unitsDto.stream().map(dto -> {
            UnitEntity existUnit = unitsRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Unit not found with id: " + dto.getId()));
            existUnit.setName(dto.getName());
            existUnit.setStatus(dto.getStatus());
            existUnit.setFloor(dto.getFloor());
            return unitsRepository.save(existUnit);
        }).toList();
    }

    // ---- Delete multiple units ----
    @Transactional
    public void deleteUnits(List<Long> unitIds) {
        for (Long id : unitIds) {
            UnitEntity existUnit = unitsRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Unit not found with id: " + id));
            unitsRepository.delete(existUnit);
        }
    }
}
