package genftw.core.match;

import genftw.api.Where;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

/**
 * Matches elements against given criteria.
 */
public class ElementMatcher {

    private final Elements elementUtils;
    private final MetaDataMatcher metaDataMatcher;

    public ElementMatcher(Elements elementUtils) {
        this.elementUtils = elementUtils;
        this.metaDataMatcher = new MetaDataMatcher(elementUtils);
    }

    public boolean matches(Element elm, Where def) {
        boolean result = true;

        // Match by kind
        if (result && def.kind().length > 0) {
            Set<ElementKind> restrictedKinds = new HashSet<ElementKind>(Arrays.asList(def.kind()));
            result = result && restrictedKinds.contains(elm.getKind());
        }

        // Match by modifiers
        if (result && def.modifiers().length > 0) {
            Set<Modifier> mandatoryModifiers = new HashSet<Modifier>(Arrays.asList(def.modifiers()));
            result = result && elm.getModifiers().containsAll(mandatoryModifiers);
        }

        // Match by simple name
        if (result && !Where.DONT_MATCH.equals(def.simpleNameMatches())) {
            result = result && elm.getSimpleName().toString().matches(def.simpleNameMatches());
        }

        // Match by annotations
        if (result && def.annotations().length > 0) {
            Set<String> mandatoryAnnotationNames = new HashSet<String>(Arrays.asList(def.annotations()));

            Set<String> elementAnnotationNames = new HashSet<String>();
            for (AnnotationMirror a : elementUtils.getAllAnnotationMirrors(elm)) {
                elementAnnotationNames.add(a.getAnnotationType().toString());
            }

            result = result && elementAnnotationNames.containsAll(mandatoryAnnotationNames);
        }

        // Match by meta-data
        if (result && !Where.DONT_MATCH.equals(def.metaData())) {
            result = result && metaDataMatcher.matches(elm, def.metaData());
        }

        return result;
    }

}
