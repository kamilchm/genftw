package genftw.core;

import freemarker.template.TemplateException;
import genftw.api.ForAllElements;
import genftw.api.ForEachElement;
import genftw.api.Produces;
import genftw.api.Where;
import genftw.core.match.ElementFinder;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileManager.Location;

/**
 * Represents a generator method ready for processing.
 */
public class GeneratorMethod {

    private final ExecutableElement element;
    private final Elements elementUtils;
    private final ElementFinder elementFinder;
    private final ProcessorLogger logger;

    public GeneratorMethod(ExecutableElement element, Elements elementUtils,
            ElementFinder elementFinder, ProcessorLogger logger) {
        this.element = element;
        this.elementUtils = elementUtils;
        this.elementFinder = elementFinder;
        this.logger = logger;
    }

    public void process(GeneratorMethodTemplate methodTemplate) throws IOException, TemplateException {
        if (getGroupMatchAnnotation() != null) {
            processGroupMatchMethod(methodTemplate);
        } else if (getLoopMatchAnnotation() != null) {
            processLoopMatchMethod(methodTemplate);
        } else {
            processSimpleMethod(methodTemplate);
        }
    }

    void processSimpleMethod(GeneratorMethodTemplate methodTemplate) throws IOException, TemplateException {
        methodTemplate.process(getOutputRootLocation(), getOutputFile());
    }

    void processGroupMatchMethod(GeneratorMethodTemplate methodTemplate) throws IOException, TemplateException {
        for (Where def : getGroupMatchAnnotation().value()) {
            Element[] matchedElements = elementFinder.getElementsFound(def);
            methodTemplate.setRootModelMapping(def.matchResultVariable(), matchedElements);
        }

        methodTemplate.process(getOutputRootLocation(), getOutputFile());
    }

    void processLoopMatchMethod(GeneratorMethodTemplate methodTemplate) throws IOException, TemplateException {
        Where def = getLoopMatchAnnotation().value();
        Element[] matchedElements = elementFinder.getElementsFound(def);

        for (Element e : matchedElements) {
            methodTemplate.setRootModelMapping(def.matchResultVariable(), e);
            methodTemplate.process(getOutputRootLocation(), resolveOutputFile(e, getOutputFile()));
        }

        if (matchedElements.length == 0) {
            logger.warning("No element(s) matched", getElement());
        }
    }

    String resolveOutputFile(Element elm, String outputFileWithVariables) {
        String result = outputFileWithVariables;

        result = result.replace("{elementSimpleName}", elm.getSimpleName());

        result = result.replace("{packageElementPath}",
                elementUtils.getPackageOf(elm).getQualifiedName().toString().replace(".", "/"));

        return result;
    }

    public ExecutableElement getElement() {
        return element;
    }

    public String getOutputFile() {
        return getOutputAnnotation().output();
    }

    public Location getOutputRootLocation() {
        return getOutputAnnotation().outputRootLocation();
    }

    public String getTemplateFile() {
        return getOutputAnnotation().template();
    }

    public Where[] getMatchDefinitions() {
        if (getGroupMatchAnnotation() != null) {
            return getGroupMatchAnnotation().value();
        } else if (getLoopMatchAnnotation() != null) {
            return new Where[] { getLoopMatchAnnotation().value() };
        } else {
            return new Where[0];
        }
    }

    Produces getOutputAnnotation() {
        return getElement().getAnnotation(Produces.class);
    }

    ForAllElements getGroupMatchAnnotation() {
        return getElement().getAnnotation(ForAllElements.class);
    }

    ForEachElement getLoopMatchAnnotation() {
        return getElement().getAnnotation(ForEachElement.class);
    }

}
