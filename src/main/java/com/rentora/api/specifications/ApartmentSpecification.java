package com.rentora.api.specifications;

import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentUser;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ApartmentSpecification {
    //name
    public static Specification<Apartment> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) return null;

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    //status
    public static Specification<Apartment> hasStatus(Apartment.ApartmentStatus apartmentStatus) {
        return (root,query,criteriaBuilder)-> apartmentStatus == null ? null : criteriaBuilder.equal(root.get("status"), apartmentStatus);
    }

    public static Specification<Apartment> hasUserId(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;

            // Join apartmentUsers
            Join<Apartment, ApartmentUser> join = root.join("apartmentUsers", JoinType.INNER);

            // Filter by userId and isActive = true
            return cb.and(
                    cb.equal(join.get("user").get("id"), userId),
                    cb.isTrue(join.get("isActive"))
            );
        };
    }
}
