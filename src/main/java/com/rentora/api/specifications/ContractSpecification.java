package com.rentora.api.specifications;

import com.rentora.api.model.entity.Contract;
import com.rentora.api.model.entity.Utility;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ContractSpecification {
    public static Specification<Utility> hasId(UUID id) {
        return (root, query, criteriaBuilder) -> {
            if (id == null ) return null;
            return criteriaBuilder.equal(root.get("id"), id);
        };
    }

    public static Specification<Utility> hasStatus(Contract.ContractStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null ) return null;
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
