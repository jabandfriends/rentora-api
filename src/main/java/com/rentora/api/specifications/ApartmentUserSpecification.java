package com.rentora.api.specifications;


import com.rentora.api.model.entity.Apartment;
import com.rentora.api.model.entity.ApartmentUser;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ApartmentUserSpecification {
    public static Specification<ApartmentUser> hasApartmentId(UUID apartmentId) {
        return (root, query, criteriaBuilder) -> {
            if (apartmentId == null) return null;

            return criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId);
        };
    }
    public static Specification<ApartmentUser> hasName(String name) {
        return (root,query,criteriaBuilder)-> name == null ? null : criteriaBuilder.like(root.get("user").get("firstName"), "%" + name + "%");
    }

    public static Specification<ApartmentUser> isActive() {
        return (root,query,criteriaBuilder)-> criteriaBuilder.equal(root.get("isActive"), true);
    }
}
