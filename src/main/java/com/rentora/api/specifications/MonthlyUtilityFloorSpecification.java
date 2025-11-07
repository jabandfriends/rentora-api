package com.rentora.api.specifications;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import org.springframework.data.jpa.domain.Specification;

public class MonthlyUtilityFloorSpecification {

    public static Specification<Floor> hasFloorName(String floorName) {

        return  ((root, query, criteriaBuilder) ->  {
            if (floorName == null || floorName.isBlank()) return null;

            return criteriaBuilder.like(criteriaBuilder.lower(root.get("floorName")), "%" + floorName.toLowerCase() + "%");
        });
    }

    public static Specification<Floor> hasBuilding(Building building) {
        return (root, query, cb) -> {
            if (building == null) {
                return null;
            }
            return cb.equal(root.get("building"), building);
        };
    }

    public static Specification<Floor> hasApartment(Apartment apartment) {
        return (root, query, cb) -> {
            if (apartment == null) {
                return null;
            }
            return cb.equal(root.join("building").join("apartment").get("id"), apartment.getId());
        };
    }

}