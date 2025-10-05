package com.rentora.api.specifications;

import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ReportSpecification {
    public static Specification<UnitUtilities> hasApartmentId(UUID apartmentId) {
        return (root,query,criteriaBuilder)-> apartmentId == null ? null : criteriaBuilder.equal(root.get("unit").get("floor").get("building").get("apartment").get("id"), apartmentId);
    }
}
