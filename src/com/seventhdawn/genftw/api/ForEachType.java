package com.seventhdawn.genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.element.TypeElement;

/**
 * Allows an annotated {@code Generator} method to be invoked for each type that matches given criteria.
 * <p>
 * Annotated methods can optionally declare a {@link TypeElement} parameter that corresponds to the type being matched.
 * <p>
 * In addition to its matching behavior, {@code ForEachType} allows you to use special built-in {@code file} and
 * {@code using} path variables for use with the {@link Generates} annotation:
 * <p>
 * <ul>
 * <li>{@code typePath}, which corresponds to {@link TypeInfo#getPath}
 * <li>{@code typeSimpleName}, which corresponds to {@link TypeInfo#getSimpleName}
 * <li>{@code typeFullName}, which corresponds to {@link TypeInfo#getFullName}
 * <li>{@code typePackageName}, which corresponds to {@link TypeInfo#getPackageName}
 * </ul>
 * 
 * @see Generator
 * @see Generates
 * @see TypeMatchCriteria
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ForEachType {

    /**
     * Type matching criteria to use.
     */
    TypeMatchCriteria value();

}
