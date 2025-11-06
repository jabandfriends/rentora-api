package com.rentora.api.specifications;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import org.springframework.data.jpa.domain.Specification;

public class MonthlyUtilityBuildingSpecification {

    public static Specification<Building> hasName(String name) {

        return  ((root, query, criteriaBuilder) ->  {
            if (name == null || name.isBlank()) return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        });
    }

    public static Specification<Building> hasApartment(Apartment apartment) {
        return (root, query, cb) -> {
            if (apartment == null) {
                return null; // ไม่กรองอะไรเลย
            }
            return cb.equal(root.get("apartment"), apartment);
        };
    }

}