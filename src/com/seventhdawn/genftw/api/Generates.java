package com.seventhdawn.genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.tools.StandardLocation;

import com.seventhdawn.genftw.spi.TemplateEngine;

/**
 * Designates a {@code Generator} method.
 * <p>
 * Normally, each generator method produces one output file. When the annotated method fails (e.g. throws an exception),
 * no file is produced. Note that the order of generator method invocation is undefined.
 * <p>
 * There are two possible return types a generator method can have:
 * <p>
 * <ul>
 * <li>{@code boolean}, in which case the output is produced when the method returns {@code true}
 * <li>{@code void}, in which case the output is always produced, unless the method throws an exception (equivalent to
 * returning {@code true})
 * </ul>
 * <p>
 * Generator methods are allowed to have very flexible signatures. Any extra parameters that are not supported by the
 * framework itself will be resolved by the given {@link TemplateEngine} implementation.
 * 
 * @see Generator
 * @see ForEachType
 * @see ForAllTypes
 * @see GeneratorContext
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Generates {

    /**
     * Path to generated output file.
     * <p>
     * Allows the use of variables in form of <tt>{myVariable}</tt>.
     */
    String file();

    /**
     * Path to template file used for generating given output file.
     * <p>
     * Allows the use of variables in form of <tt>{myVariable}</tt>.
     */
    String using();

    /**
     * Root location of the generated output file.
     * <p>
     * Full output file path will be the concatenation of {@linkplain #fileLocation} and {@linkplain #file}.
     * <p>
     * There are two standard output locations as declared by JSR-269 specification:
     * <p>
     * <ul>
     * <li>{@link StandardLocation#SOURCE_OUTPUT SOURCE_OUTPUT}, location of new source files
     * <li>{@link StandardLocation#CLASS_OUTPUT CLASS_OUTPUT}, location of new class files
     * </ul>
     */
    StandardLocation fileLocation() default StandardLocation.SOURCE_OUTPUT;

}
