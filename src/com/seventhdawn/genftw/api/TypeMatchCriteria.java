package com.seventhdawn.genftw.api;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines type matching criteria used with {@code ForEachType} and {@code ForAllTypes}.
 * 
 * @see ForEachType
 * @see ForAllTypes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@Documented
public @interface TypeMatchCriteria {

    /**
     * Match types that have the given annotation.
     * <p>
     * The annotation may appear directly or be inherited.
     * <p>
     * In this case, the default {@code Annotation.class} value is a null object, used to bypass annotation matching.
     */
    Class<? extends Annotation> annotatedWith() default Annotation.class;

    /**
     * Match types that are subtypes of the given type.
     * <p>
     * Any type is considered to be a subtype of itself.
     */
    Class<?> subtypeOf() default Object.class;

    /**
     * Match types whose fully qualified (canonical) name matches the given regular expression pattern.
     */
    String fullNameMatches() default ".*";

}
