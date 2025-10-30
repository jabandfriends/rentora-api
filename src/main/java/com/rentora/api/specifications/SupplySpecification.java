package com.rentora.api.specifications;

import com.rentora.api.model.entity.Supply;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class SupplySpecification {
    public static Specification<Supply> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) return null;
            return criteriaBuilder.equal(
                    root.get("apartment").get("id"),
                    apartmentId
            );
        };
    }

    public static Specification<Supply> hasName(String supplyName) {
        return (root, query, criteriaBuilder) -> {
            if (supplyName == null || supplyName.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%" + supplyName.toLowerCase() + "%");
        };
    }

    public static Specification<Supply> hasCategory(Supply.SupplyCategory category) {
        if (category == null) return null;
        return ((root, query, cb) -> cb.equal(root.get("category"), category) );
    }

    public static Specification<Supply> hasNotDelete() {
        return ((root, query, cb) -> cb.equal(root.get("isDeleted"), Boolean.FALSE) );
    }
}
