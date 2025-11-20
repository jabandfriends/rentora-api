package com.rentora.api.specifications;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Utility;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ContractSpecification {
    public static Specification<Contract> hasId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<Contract> hasTenantId(UUID tenantId) {
        return (root, query, criteriaBuilder) -> {
            if (tenantId == null ) return null;
            return criteriaBuilder.equal(root.get("tenant").get("id"), tenantId);
        };
    }

    public static Specification<Contract> hasApartmentId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("unit").get("floor").get("building").get("apartment").get("id"), id);
        };
    }

    public static Specification<Contract> hasUnitId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("unit").get("id"), id);
        };
    }

    public static Specification<Contract> hasStatus(Contract.ContractStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null ) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }


}
