package genftw.api;

import genftw.core.GeneratorProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.tools.StandardLocation;

/**
 * Designates a generator method that produces output file(s).
 * <p>
 * Signature of an annotated method is irrelevant to its processing.
 * 
 * @see ForAllElements
 * @see ForEachElement
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Produces {

    /**
     * Output file pathname, relative to {@linkplain #outputRootLocation() output root location}.
     */
    String output();

    /**
     * Output file root location.
     * <p>
     * Selected value must be an {@linkplain StandardLocation#isOutputLocation() output location}.
     */
    StandardLocation outputRootLocation() default StandardLocation.SOURCE_OUTPUT;

    /**
     * Template file pathname, relative to {@linkplain GeneratorProcessor#OPT_TEMPLATE_ROOT_DIR template root location}.
     */
    String template();

}
