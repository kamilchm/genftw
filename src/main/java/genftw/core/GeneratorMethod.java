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

        for (Where extraDef : getLoopMatchAnnotation().matchExtraElements()) {
            Element[] extraElements = elementFinder.getElementsFound(extraDef);
            methodTemplate.setRootModelMapping(extraDef.matchResultVariable(), extraElements);
        }

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
            Where[] extraDefs = getLoopMatchAnnotation().matchExtraElements();

            Where[] result = new Where[extraDefs.length + 1];
            result[0] = getLoopMatchAnnotation().value();
            System.arraycopy(extraDefs, 0, result, 1, extraDefs.length);

            return result;
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
