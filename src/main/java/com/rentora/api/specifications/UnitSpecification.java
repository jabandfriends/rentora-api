package com.rentora.api.specifications;

import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.Unit;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UnitSpecification {
    public static Specification<Unit> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) return null;
            return criteriaBuilder.equal(
                    root.get("floor").get("building").get("apartment").get("id"),
                    apartmentId
            );
        };
    }

    public static Specification<Unit> hasStatus(Unit.UnitStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<Unit> hasUnitType(Unit.UnitType unitType) {
        return (root, query, criteriaBuilder) -> {
            if (unitType == null) return null;
            return criteriaBuilder.equal(root.get("unitType"), unitType);
        };
    }

    public static Specification<Unit> hasFloorId(UUID floorId) {
        return (root, query, criteriaBuilder) -> {
            if (floorId == null) return null;
            return criteriaBuilder.equal(root.get("floor").get("id"), floorId);
        };
    }

    public static Specification<Unit> hasName(String search) {
        return (root, query, criteriaBuilder) -> {
            if (search == null || search.isEmpty()) return null;
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("unitName")), "%" + search.toLowerCase() + "%");
        };
    }
}
