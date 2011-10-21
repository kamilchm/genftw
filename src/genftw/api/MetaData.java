package genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Adds custom meta-data to arbitrary source elements.
 * <p>
 * Annotated elements can be matched by generator methods using {@linkplain Where#metaData() meta-data match string}.
 * <p>
 * When matching elements, this annotation may appear directly on an element, or be nested as a meta-annotation present
 * on some other element annotation.
 * 
 * @see Where
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MetaData {

    /**
     * An optional kind that defines stereotype of the annotated element.
     */
    String kind() default "";

    /**
     * Meta-data properties, each in {@code name} or {@code name=value} format.
     */
    String[] properties() default {};

}
