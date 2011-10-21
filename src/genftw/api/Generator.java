package genftw.api;

import genftw.core.GeneratorProcessor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that an annotated interface describes a generator.
 * <p>
 * Generators are used to {@linkplain Produces produce} arbitrary files, such as additional source code, configuration
 * files, etc. Generators are {@linkplain GeneratorProcessor processed} before invoking Java compiler, which means your
 * handwritten source code can directly reference generated source elements.
 * 
 * @see Produces
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Generator {

}
