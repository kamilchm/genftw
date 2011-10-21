package genftw.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import genftw.core.util.ElementGoodies;

/**
 * Runtime environment for processing generator methods.
 */
public class GeneratorMethodEnvironment {

    private final Configuration templateConfig;
    private final Filer filer;
    private final ProcessorLogger logger;
    private final ElementGoodies elementGoodies;

    public GeneratorMethodEnvironment(Configuration templateConfig,
            Filer filer, Elements elementUtils, ProcessorLogger logger) {
        this.templateConfig = templateConfig;
        this.filer = filer;
        this.logger = logger;
        this.elementGoodies = new ElementGoodies(elementUtils);
    }

    public void process(GeneratorMethod method) throws IOException, TemplateException {
        Element methodElement = method.getElement();
        logger.info("Processing generator method " + methodElement.getSimpleName(), methodElement);

        if (!method.getOutputRootLocation().isOutputLocation()) {
            logger.error("Output file root location is not an output location", methodElement);
            return;
        }

        // Load template
        Template template = templateConfig.getTemplate(method.getTemplateFile());

        // Create template root data-model
        Map<String, Object> rootMap = new HashMap<String, Object>();
        rootMap.put("elementGoodies", elementGoodies);

        // Process generator method
        method.process(new GeneratorMethodTemplate(filer, template, rootMap));
    }

}
