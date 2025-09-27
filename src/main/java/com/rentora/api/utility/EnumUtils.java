package com.rentora.api.utility;

import com.rentora.api.model.entity.Unit;

public class EnumUtils {

    public static Unit.UnitType parseUnitType(String unitType) {
        if (unitType == null || unitType.isEmpty()) {
            return null;
        }
        try {
            return Unit.UnitType.valueOf(unitType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid unitType: " + unitType);
        }
    }
}
