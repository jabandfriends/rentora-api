package com.rentora.api.specifications;

import com.rentora.api.model.entity.UnitUtilities;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class ReportSpecification {
    public static Specification<UnitUtilities> hasApartmentId(UUID apartmentId) {
        return (root,query,criteriaBuilder)-> apartmentId == null ? null : criteriaBuilder.equal(root.get("unit").get("floor").get("building").get("apartment").get("id"), apartmentId);
    }

    public static Specification<UnitUtilities> hasName(String unitName) {
        return (root,query,criteriaBuilder)-> unitName == null || unitName.isEmpty() ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("unit").get("unitName")), "%" + unitName.toLowerCase() + "%");
    }

    public static Specification<UnitUtilities> hasBuildingName(String buildingName) {
        return (root,query,criteriaBuilder)-> buildingName == null || buildingName.isEmpty() ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("unit").get("floor").get("building").get("name")), "%" + buildingName.toLowerCase() + "%");
    }

    public static Specification<UnitUtilities> matchReadingDate(LocalDate readingDate) {
        return (root,query,criteriaBuilder)-> readingDate == null ? null : criteriaBuilder.equal(root.get("readingDate"), readingDate);
    }
}
