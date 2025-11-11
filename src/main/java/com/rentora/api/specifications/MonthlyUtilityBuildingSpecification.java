package com.rentora.api.specifications;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class MonthlyUtilityBuildingSpecification {

    public static Specification<Building> hasBuildingId(UUID BuildingId) {

        return  ((root, query, cb) ->  {
            if (BuildingId == null){
                return cb.isNull(root.get("id"));
            }

            return cb.equal(root.get("id"), BuildingId);
        });
    }

    public static Specification<Building> hasApartment(Apartment apartment) {
        return (root, query, cb) -> {
            if (apartment == null) {
                return null;
            }
            return cb.equal(root.get("apartment"), apartment);
        };
    }

}