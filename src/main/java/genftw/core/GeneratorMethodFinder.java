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

import genftw.api.ForAllElements;
import genftw.api.ForEachElement;
import genftw.api.Produces;
import genftw.core.match.ElementFinder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;

/**
 * Element visitor that scans generator types, looking for generator methods.
 */
public class GeneratorMethodFinder extends ElementScanner6<Void, Void> {

    private final Elements elementUtils;
    private final ElementFinder elementFinder;
    private final ProcessorLogger logger;
    private final List<GeneratorMethod> methodsFound;

    public GeneratorMethodFinder(Elements elementUtils, ElementFinder elementFinder, ProcessorLogger logger) {
        this.elementUtils = elementUtils;
        this.elementFinder = elementFinder;
        this.logger = logger;
        this.methodsFound = new LinkedList<GeneratorMethod>();
    }

    public Void scan(Set<TypeElement> types) {
        methodsFound.clear();
        return super.scan(types, null);
    }

    public GeneratorMethod[] getMethodsFound() {
        return methodsFound.toArray(new GeneratorMethod[0]);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Void p) {
        if (e.getKind() == ElementKind.METHOD && e.getAnnotation(Produces.class) != null) {
            logger.info("Found generator method " + e.getSimpleName(), e);

            if (e.getAnnotation(ForAllElements.class) != null
                    && e.getAnnotation(ForEachElement.class) != null) {
                logger.error("Cannot use more than one element matching annotation", e);
            } else {
                methodsFound.add(new GeneratorMethod(e, elementUtils, elementFinder, logger));
            }

            if (e.getReturnType().getKind() != TypeKind.VOID || !e.getParameters().isEmpty()
                    || !e.getThrownTypes().isEmpty() || !e.getTypeParameters().isEmpty()) {
                logger.warning("Signature of a generator method is irrelevant to its processing", e);
            }
        }

        return null;
    }

    @Override
    public Void visitType(TypeElement e, Void p) {
        logger.info("Scanning " + e.getQualifiedName().toString() + " for generator methods", e);
        return super.visitType(e, p);
    }

    @Override
    public Void visitPackage(PackageElement e, Void p) {
        // Don't visit package elements
        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, Void p) {
        // Don't visit variable elements
        return null;
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Void p) {
        // Don't visit type parameter elements
        return null;
    }

}
