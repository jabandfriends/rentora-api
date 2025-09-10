package com.rentora.api.service;

import com.rentora.api.entity.ApartmentsEntity;
import com.rentora.api.exception.ResourceNotFoundException;
import com.rentora.api.repository.ApartmentsRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ApartmentsService {

    private final ApartmentsRepository apartmentRepository;

    public ApartmentsService(ApartmentsRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    public Page<ApartmentsEntity> getAll(String keyword,Pageable pageable) {
        Page<ApartmentsEntity> page;

        if (keyword != null && !keyword.isEmpty()) {
            // Search by name
            page = apartmentRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            // Fetch all
            page = apartmentRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            throw new ResourceNotFoundException("No apartments found");
        }

        return page;
    }


    public ApartmentsEntity getById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartment not found with id: " + id));
    }

    public ApartmentsEntity create(ApartmentsEntity apartment) {
        return apartmentRepository.save(apartment);
    }

    public ApartmentsEntity update(ApartmentsEntity apartmentDetails) {
        Long id = apartmentDetails.getId();
        ApartmentsEntity existApartment = apartmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Apartment not found"));;

        //update field
        existApartment.setName(apartmentDetails.getName());
        existApartment.setAddress(apartmentDetails.getAddress());

        return apartmentRepository.save(existApartment);
    }

    public void delete(Long id) {
        ApartmentsEntity apartment = getById(id);
        apartmentRepository.delete(apartment);
    }
}