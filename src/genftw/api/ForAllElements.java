package genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Causes a generator method to be processed once, providing all elements matching given criteria.
 * 
 * @see Where
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ForAllElements {

    /**
     * Source element match criteria.
     */
    Where[] value() default @Where;

}
