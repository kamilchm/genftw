package genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

/**
 * Defines source element match criteria.
 * <p>
 * Matched element(s) will be a specialization of the {@link Element} interface.
 * 
 * @see MetaData
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@Documented
public @interface Where {

    // Should be an invalid regular expression pattern
    String DONT_MATCH = "***";

    /**
     * Name of template variable that will hold the match result.
     */
    String matchResultVariable() default "matchResult";

    /**
     * Element kind(s) to match.
     * <p>
     * All standard element kinds are supported. Empty array means no element kind restrictions.
     */
    ElementKind[] kind() default {};

    /**
     * Filter elements that have given modifiers.
     */
    Modifier[] modifiers() default {};

    /**
     * Filter elements whose {@linkplain Element#getSimpleName() simple name} matches given regular expression pattern.
     */
    String simpleNameMatches() default DONT_MATCH;

    /**
     * Filter elements that have given annotations, defined by their fully qualified names.
     * <p>
     * Annotations may appear directly or be inherited.
     */
    String[] annotations() default {};

    /**
     * {@linkplain MetaData Meta-data} match string.
     * <p>
     * Always prefer meta-data matching over custom filter logic.
     * <p>
     * Examples:
     * <p>
     * <ul>
     * <li>match elements of kind K: <tt>K</tt>
     * <li>match elements of any kind: <tt>*</tt>
     * <li>match elements of kind K, having property P: <tt>K[P]</tt>
     * <li>match elements of kind K, having property P with value V: <tt>K[P=V]</tt>
     * <li>match elements of kind K, having properties P and Q: <tt>K[P][Q]</tt>
     * </ul>
     */
    String metaData() default DONT_MATCH;

}
