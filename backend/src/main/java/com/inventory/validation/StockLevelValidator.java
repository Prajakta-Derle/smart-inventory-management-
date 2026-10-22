package com.inventory.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StockLevelValidator
        implements ConstraintValidator<StockLevelValid, StockLevelProvider> {

    @Override
    public boolean isValid(StockLevelProvider value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null handled by @NotNull on the bean itself
        }
        // Negative values are reported by @Min on the fields; this constraint
        // focuses only on the min <= max relationship (Requirement 3.11).
        if (!isValidRelationship(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("minStockLevel")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean isValidRelationship(StockLevelProvider value) {
        return value.getMinStockLevel() <= value.getMaxStockLevel();
    }
}
