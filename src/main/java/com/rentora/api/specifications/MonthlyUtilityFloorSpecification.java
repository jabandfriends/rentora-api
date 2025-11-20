package com.rentora.api.specifications;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.Building;
import com.rentora.api.model.entity.Floor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class MonthlyUtilityFloorSpecification {

    public static Specification<Floor> hasFloorId(UUID floorId) {
        return (root, query, cb) -> {
            if (floorId == null) {
                return cb.isTrue(cb.literal(true));
            }
            // กรองตาม ID Primary Key ของ Floor Entity
            return cb.equal(root.get("id"), floorId);
        };
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