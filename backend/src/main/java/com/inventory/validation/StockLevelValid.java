package com.inventory.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Class-level constraint asserting that minStockLevel &le; maxStockLevel
 * on a product request DTO (Requirement 3.11).
 */
@Documented
@Constraint(validatedBy = StockLevelValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface StockLevelValid {

    String message() default "Minimum stock level must not exceed maximum stock level";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
