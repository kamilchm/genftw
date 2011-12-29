/*
 * Copyright 2011 GenFTW contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package genftw.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModelException;
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
        Map<String, Object> rootMap = createTemplateRootModel();

        // Process generator method
        method.process(new GeneratorMethodTemplate(filer, template, rootMap, logger));
    }

    Map<String, Object> createTemplateRootModel() throws TemplateModelException {
        Map<String, Object> rootMap = new HashMap<String, Object>();

        // Expose ElementGoodies instance reference
        rootMap.put("elementGoodies", elementGoodies);

        // Expose ElementFilter static reference
        rootMap.put("ElementFilter", BeansWrapper.getDefaultInstance()
                .getStaticModels().get("javax.lang.model.util.ElementFilter"));

        // Expose all available enum classes
        rootMap.put("enums", BeansWrapper.getDefaultInstance().getEnumModels());

        return rootMap;
    }

}
