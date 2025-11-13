package com.rentora.api.specifications;

import com.rentora.api.model.entity.ApartmentService;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ServiceSpecification {
    public static Specification<ApartmentService> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) return null;
            return criteriaBuilder.equal(
                    root.get("floor").get("building").get("apartment").get("id"),
                    apartmentId
            );
        };
    }
}
