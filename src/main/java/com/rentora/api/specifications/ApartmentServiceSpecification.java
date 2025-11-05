package com.rentora.api.specifications;

import com.rentora.api.model.entity.ApartmentService;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ApartmentServiceSpecification {
    public static Specification<ApartmentService> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) return null;
            return criteriaBuilder.equal(
                    root.get("apartment").get("id"),
                    apartmentId
            );
        };
    }

    public static Specification<ApartmentService> isActive(Boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive == null) return null;
            return criteriaBuilder.equal(
                    root.get("isActive"),
                    isActive
            );
        };
    }
}
