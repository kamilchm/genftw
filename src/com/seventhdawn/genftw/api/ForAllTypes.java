package com.seventhdawn.genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.element.TypeElement;

/**
 * Allows an annotated {@code Generator} method to be invoked for all types that match given criteria.
 * <p>
 * Annotated methods can optionally declare a {@link TypeElement} array parameter that corresponds to all types being
 * matched. When invoked, the {@code TypeElement} array is guaranteed to be non-empty.
 * 
 * @see Generator
 * @see Generates
 * @see TypeMatchCriteria
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ForAllTypes {

    /**
     * Type matching criteria to use.
     */
    TypeMatchCriteria value();

}
