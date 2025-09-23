package com.rentora.api.specifications;

import com.rentora.api.model.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.UUID;

public class MaintenanceSpecification {
    public static Specification<Maintenance> hasName(String name) {
        return (root,query,criteriaBuilder)-> name == null ? null : criteriaBuilder.like(root.get("title").as(String.class), "%" + name + "%");
    }

    public static Specification<Maintenance> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) {
                return null;
            }
            // Join Maintenance -> Unit -> floor -> Building
            Join<Maintenance, Unit> unitJoin = root.join("unit", JoinType.INNER);
            Join<Unit, Floor> floorJoin = unitJoin.join("floor", JoinType.INNER);
            Join<Floor, Building> buildingJoin = floorJoin.join("building", JoinType.INNER);

            return criteriaBuilder.equal(buildingJoin.get("apartment").get("id"), apartmentId);
        };
    }

    public static Specification<Maintenance> hasRoomOrTenantName(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(searchTerm)) {
                return null;
            }
            String likeTerm = "%" + searchTerm.toLowerCase() + "%";

            // Join Maintenance -> Unit
            Join<Maintenance, Unit> unitJoin = root.join("unit", JoinType.INNER);

            Predicate roomPredicate = criteriaBuilder.like(criteriaBuilder.lower(unitJoin.get("roomNumber")), likeTerm);
            Predicate tenantNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("tenantUser").get("firstName")), likeTerm);

            return criteriaBuilder.or(roomPredicate, tenantNamePredicate);
        };
    }

    public static Specification<Maintenance> hasStatus(Maintenance.Status status) {
        return (root, query, criteriaBuilder) -> status == null ? null : criteriaBuilder.equal(root.get("status"), status);
    }
}