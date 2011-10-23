package genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.element.Element;

/**
 * Causes a generator method to be processed multiple times, providing each element matching given criteria.
 * <p>
 * Allows the use of following variables within {@linkplain Produces#output() output file pathname}:
 * <p>
 * <ul>
 * <li>{@code elementSimpleName}: {@linkplain Element#getSimpleName() simple name} of the matched element
 * <li>{@code packageElementPath}: relative pathname of the package element that encloses matched element
 * </ul>
 * 
 * @see Where
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ForEachElement {

    /**
     * Source element match criteria.
     */
    Where value() default @Where;

    /**
     * Additional elements to match and provide to the template.
     */
    Where[] matchExtraElements() default {};

}
