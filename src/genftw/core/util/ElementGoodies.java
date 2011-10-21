package genftw.core.util;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

/**
 * Contains utility methods for working with source elements.
 * <p>
 * Intended for use within templates.
 */
public class ElementGoodies {

    private final Elements elementUtils;

    public ElementGoodies(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public String packageOf(Element elm) {
        return elementUtils.getPackageOf(elm).getQualifiedName().toString();
    }

    // TODO add more utility methods here

}
