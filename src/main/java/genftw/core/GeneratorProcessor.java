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

import freemarker.cache.StrongCacheStorage;
import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import genftw.api.Generator;
import genftw.api.Where;
import genftw.core.match.ElementFinder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * JSR-269 annotation processor that interprets {@linkplain Generator generators}.
 * 
 * @see Generator
 */
@SupportedAnnotationTypes("genftw.api.Generator")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions({
        GeneratorProcessor.OPT_VERBOSE,
        GeneratorProcessor.OPT_MATCHED_ELEMENT_PACKAGE_PATTERN,
        GeneratorProcessor.OPT_TEMPLATE_VERBOSE,
        GeneratorProcessor.OPT_TEMPLATE_ROOT_DIR,
        GeneratorProcessor.OPT_TEMPLATE_DEFAULT_ENCODING })
public class GeneratorProcessor extends AbstractProcessor {

    /**
     * This option enables verbose messages during generator processing.
     */
    public static final String OPT_VERBOSE = "genftw.verbose";

    /**
     * This option defines regular expression pattern that filters {@linkplain RoundEnvironment#getRootElements() root
     * elements} (and elements enclosed within them), that are eligible for {@linkplain Where matching}, by their
     * enclosing {@linkplain PackageElement#getQualifiedName() package name}.
     * <p>
     * <em>It is strongly recommended to set this option to a sensible value.</em>
     */
    public static final String OPT_MATCHED_ELEMENT_PACKAGE_PATTERN = "genftw.matchedElementPackagePattern";

    /**
     * This option enables FreeMarker logging using {@code java.util.logging} package.
     */
    public static final String OPT_TEMPLATE_VERBOSE = "genftw.templateVerbose";

    /**
     * This option defines template file root directory.
     * <p>
     * Selected value must be a pathname that points to an existing directory.
     */
    public static final String OPT_TEMPLATE_ROOT_DIR = "genftw.templateRootDir";

    /**
     * This option overrides default template file encoding.
     * <p>
     * Default value is {@linkplain Charset#defaultCharset() JVM default charset}.
     */
    public static final String OPT_TEMPLATE_DEFAULT_ENCODING = "genftw.templateDefaultEncoding";

    private ProcessorLogger logger;
    private ElementFinder elementFinder;
    private GeneratorMethodFinder methodFinder;
    private GeneratorMethodEnvironment methodEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Map<String, String> options = processingEnv.getOptions();

        boolean verbose = options.containsKey(OPT_VERBOSE);

        Pattern elementPackagePattern = options.containsKey(OPT_MATCHED_ELEMENT_PACKAGE_PATTERN)
                ? Pattern.compile(options.get(OPT_MATCHED_ELEMENT_PACKAGE_PATTERN))
                : Pattern.compile(".*");

        boolean templateVerbose = options.containsKey(OPT_TEMPLATE_VERBOSE);

        String templateRootDir = options.get(OPT_TEMPLATE_ROOT_DIR);
        if (templateRootDir == null) {
            throw new IllegalArgumentException("Template root directory not defined");
        }

        String defaultEncoding = options.containsKey(OPT_TEMPLATE_DEFAULT_ENCODING)
                ? options.get(OPT_TEMPLATE_DEFAULT_ENCODING)
                : Charset.defaultCharset().name();

        logger = createLogger(processingEnv.getMessager(), verbose);
        elementFinder = createElementFinder(processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(), elementPackagePattern);
        methodFinder = createMethodFinder(processingEnv.getElementUtils(),
                elementFinder, logger);
        methodEnv = createMethodEnvironment(processingEnv.getFiler(),
                processingEnv.getElementUtils(), logger, templateVerbose,
                templateRootDir, defaultEncoding);

        logger.info(GeneratorProcessor.class.getSimpleName()
                + " initialized, using FreeMarker version " + Configuration.getVersionNumber());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty() || roundEnv.processingOver()) {
            return false;
        }

        // Find generator methods
        methodFinder.scan(getGeneratorElements(roundEnv));
        GeneratorMethod[] methodsFound = methodFinder.getMethodsFound();

        // Scan source elements by generator method match criteria
        elementFinder.scan(roundEnv.getRootElements(), getMatchDefinitions(methodsFound));

        // Process generator methods
        for (GeneratorMethod m : methodsFound) {
            try {
                methodEnv.process(m);
            } catch (Exception e) {
                logger.error("Error while processing generator method", e, m.getElement());
            }
        }

        return true;
    }

    /**
     * Returns valid generator types for further processing.
     */
    Set<TypeElement> getGeneratorElements(RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(Generator.class);
        Set<TypeElement> generatorElements = new HashSet<TypeElement>(annotatedElements.size());

        for (Element e : annotatedElements) {
            logger.info("Found element " + e.toString(), e);

            if (!TypeElement.class.isAssignableFrom(e.getClass())) {
                // This shouldn't happen, Generator annotation is permitted on types only
                continue;
            }

            if (e.getKind() != ElementKind.INTERFACE) {
                logger.error(Generator.class.getSimpleName() + " annotation allowed only on interfaces", e);
                continue;
            }

            generatorElements.add((TypeElement) e);
        }

        return generatorElements;
    }

    /**
     * Returns all match definitions declared by generator methods.
     */
    Set<Where> getMatchDefinitions(GeneratorMethod[] methods) {
        Map<Integer, Where> matchDefinitions = new HashMap<Integer, Where>();

        for (GeneratorMethod m : methods) {
            for (Where def : m.getMatchDefinitions()) {
                matchDefinitions.put(elementFinder.getKey(def), def);
            }
        }

        return new HashSet<Where>(matchDefinitions.values());
    }

    ProcessorLogger createLogger(Messager messager, boolean verbose) {
        return new ProcessorLogger(messager, verbose);
    }

    ElementFinder createElementFinder(Elements elementUtils,
            Types typeUtils, Pattern elementPackagePattern) {
        return new ElementFinder(elementUtils, typeUtils, elementPackagePattern);
    }

    GeneratorMethodFinder createMethodFinder(Elements elementUtils,
            ElementFinder elementFinder, ProcessorLogger logger) {
        return new GeneratorMethodFinder(elementUtils, elementFinder, logger);
    }

    GeneratorMethodEnvironment createMethodEnvironment(Filer filer,
            Elements elementUtils, ProcessorLogger logger, boolean templateVerbose,
            String templateRootDir, String defaultEncoding) {
        // Configure FreeMarker logging
        try {
            if (templateVerbose) {
                Logger.selectLoggerLibrary(Logger.LIBRARY_JAVA);
            } else {
                Logger.selectLoggerLibrary(Logger.LIBRARY_NONE);
            }
        } catch (ClassNotFoundException e) {
            // This shouldn't happen, ignore exception
        }

        Configuration templateConfig = new Configuration();

        // Configure template root directory
        try {
            templateConfig.setDirectoryForTemplateLoading(new File(templateRootDir));
        } catch (IOException e) {
            throw new IllegalArgumentException(logger.formatErrorMessage(
                    "Error while setting template root directory", e), e);
        }

        // Cache templates using strong references for efficiency
        templateConfig.setCacheStorage(new StrongCacheStorage());

        // Cache templates forever
        templateConfig.setTemplateUpdateDelay(Integer.MAX_VALUE);

        // Apply common template settings
        templateConfig.setObjectWrapper(new DefaultObjectWrapper());
        templateConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        templateConfig.setDefaultEncoding(defaultEncoding);
        templateConfig.setLocalizedLookup(false);

        return new GeneratorMethodEnvironment(templateConfig, filer, elementUtils, logger);
    }

}
