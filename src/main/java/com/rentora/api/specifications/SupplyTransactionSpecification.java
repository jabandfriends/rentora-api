package com.rentora.api.specifications;

import com.rentora.api.model.entity.SupplyTransaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class SupplyTransactionSpecification {
    public static Specification<SupplyTransaction> hasApartmentId(UUID apartmentId) {
        if (apartmentId == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("supply").get("apartment").get("id"), apartmentId) );
    }

    public static Specification<SupplyTransaction> hasCategory(SupplyTransaction.SupplyTransactionType supplyTransactionType) {
        if (supplyTransactionType == null) return null;
        return ((root, query, cb) ->
                cb.equal(root.get("transactionType"), supplyTransactionType) );
    }

    public static Specification<SupplyTransaction> hasSupplyName(String supplyName) {
        if (supplyName == null || supplyName.isEmpty()) return null;
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("supply").get("name")),"%"+ supplyName.toLowerCase() + "%") );
    }
}
