package com.rentora.api.specifications;

import com.rentora.api.model.entity.UnitUtilities;
import com.rentora.api.model.entity.Utility;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UtilitySpecification {
    public static Specification<Utility> hasUtilityName(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("utilityName")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<Utility> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null ) return null;
            return criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId);
        };
    }
}
