package com.seventhdawn.genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows an annotated method to be invoked before processing each {@code Generator} method.
 * <p>
 * Annotated methods must return {@code void} and have no arguments. When the annotated method fails (e.g. throws an
 * exception), processing of the corresponding generator will be halted. Note that there is no order defined for
 * executing annotated methods.
 * 
 * @see Generator
 * @see Generates
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface BeforeEach {

}
