package com.rentora.api.specifications;

import com.rentora.api.model.entity.Unit;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UnitUtilitySpecification {
    public static Specification<UnitUtilities> hasUtilityId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("utility").get("id"),  id);
        };
    }
    public static Specification<UnitUtilities> hasId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("id"),  id);
        };
    }
    public static Specification<UnitUtilities> hasUnitId(UUID unitId) {
        return (root, query, criteriaBuilder) -> {
            if (unitId == null ) return null;
            return criteriaBuilder.equal(root.get("unit").get("id"),  unitId );
        };
    }
    public static Specification<UnitUtilities> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null ) return null;
            return criteriaBuilder.equal(root.get("unit").get("floor").get("building").get("apartment").get("id"),  apartmentId );
        };
    }
}
