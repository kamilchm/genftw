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
import genftw.core.match.ElementMatcher;
import genftw.core.match.MetaDataMatcher;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({
        GeneratorProcessor.OPT_MATCHED_ELEMENT_PACKAGE_FILTER,
        GeneratorProcessor.OPT_TEMPLATE_ROOT_DIR,
        GeneratorProcessor.OPT_TEMPLATE_LOGGER_LIBRARY,
        GeneratorProcessor.OPT_TEMPLATE_DEFAULT_ENCODING })
public class GeneratorProcessor extends AbstractProcessor {

    /**
     * This option defines regular expression pattern that filters {@linkplain RoundEnvironment#getRootElements() root
     * elements} (and elements enclosed within them), that are eligible for {@linkplain Where matching}, by their
     * enclosing {@linkplain PackageElement#getQualifiedName() package name}.
     * <p>
     * Matching candidate element filter is disabled by default.
     * <em>For improved performance, it is strongly recommended to set this option to a sensible value.</em>
     */
    public static final String OPT_MATCHED_ELEMENT_PACKAGE_FILTER = "genftw.matchedElementPackageFilter";

    /**
     * This option defines template file root directory.
     * <p>
     * Selected value must be a pathname that points to an existing directory.
     */
    public static final String OPT_TEMPLATE_ROOT_DIR = "genftw.templateRootDir";

    /**
     * This option controls FreeMarker logging.
     * <p>
     * Selected value must be one of FreeMarker {@link Logger} constants. FreeMarker logging is
     * {@linkplain Logger#LIBRARY_NONE disabled} by default.
     */
    public static final String OPT_TEMPLATE_LOGGER_LIBRARY = "genftw.templateLoggerLibrary";

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

        setLogger(createLogger(processingEnv.getMessager()));

        String elementPackageFilter = options.containsKey(OPT_MATCHED_ELEMENT_PACKAGE_FILTER)
                ? options.get(OPT_MATCHED_ELEMENT_PACKAGE_FILTER) : ".*";

        elementFinder = createElementFinder(processingEnv.getElementUtils(),
                        processingEnv.getTypeUtils(), elementPackageFilter);

        methodFinder = createMethodFinder(processingEnv.getElementUtils(),
                elementFinder, logger);

        String templateRootDir = options.get(OPT_TEMPLATE_ROOT_DIR);
        if (templateRootDir == null) {
            logger.warning("Template root directory not defined, using current user working directory");
            templateRootDir = System.getProperty("user.dir");
        }

        int templateLoggerLibrary = Logger.LIBRARY_NONE;
        if (options.containsKey(OPT_TEMPLATE_LOGGER_LIBRARY)) {
            try {
                templateLoggerLibrary = Integer.parseInt(options.get(OPT_TEMPLATE_LOGGER_LIBRARY));
            } catch (NumberFormatException e) {
                // Ignore exception
            }
        }

        String defaultEncoding = options.containsKey(OPT_TEMPLATE_DEFAULT_ENCODING)
                ? options.get(OPT_TEMPLATE_DEFAULT_ENCODING)
                : Charset.defaultCharset().name();

        methodEnv = createMethodEnvironment(processingEnv.getFiler(),
                processingEnv.getElementUtils(), logger, templateRootDir,
                templateLoggerLibrary, defaultEncoding);

        logger.info("GeneratorProcessor initialized, using FreeMarker " + Configuration.getVersionNumber());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty() || roundEnv.processingOver()) {
            return false;
        }

        logger.info("GeneratorProcessor started");

        // Find generator methods
        methodFinder.scan(getGeneratorElements(roundEnv));
        GeneratorMethod[] methodsFound = methodFinder.getMethodsFound();

        // Scan source elements by generator method match criteria
        Set<Where> matchDefinitions = getMatchDefinitions(methodsFound);
        if (!matchDefinitions.isEmpty()) {
            elementFinder.scan(roundEnv.getRootElements(), matchDefinitions);
        }

        // Process generator methods
        for (GeneratorMethod m : methodsFound) {
            try {
                methodEnv.process(m);
            } catch (Exception e) {
                logger.error("Error while processing generator method", e, m.getElement());
            }
        }

        logger.info("GeneratorProcessor finished");

        return true;
    }

    /**
     * Returns valid generator types for further processing.
     */
    Set<TypeElement> getGeneratorElements(RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(Generator.class);
        Set<TypeElement> generatorElements = new HashSet<TypeElement>(annotatedElements.size());

        for (Element e : annotatedElements) {
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

    void setLogger(ProcessorLogger logger) {
        this.logger = logger;
    }

    ProcessorLogger createLogger(Messager messager) {
        return new ProcessorLogger(messager);
    }

    ElementFinder createElementFinder(Elements elementUtils,
            Types typeUtils, String elementPackageFilter) {
        MetaDataMatcher metaDataMatcher = new MetaDataMatcher(elementUtils);
        ElementMatcher elementMatcher = new ElementMatcher(elementUtils, metaDataMatcher);

        return new ElementFinder(elementUtils, typeUtils, elementMatcher, elementPackageFilter);
    }

    GeneratorMethodFinder createMethodFinder(Elements elementUtils,
            ElementFinder elementFinder, ProcessorLogger logger) {
        return new GeneratorMethodFinder(elementUtils, elementFinder, logger);
    }

    GeneratorMethodEnvironment createMethodEnvironment(Filer filer,
            Elements elementUtils, ProcessorLogger logger, String templateRootDir,
            int templateLoggerLibrary, String defaultEncoding) {
        // Configure FreeMarker logging
        try {
            Logger.selectLoggerLibrary(templateLoggerLibrary);
        } catch (ClassNotFoundException e) {
            // Selected logger library not found on classpath, ignore exception
        }

        // Create FreeMarker configuration
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

        // Cache templates during annotation processor lifetime
        templateConfig.setTemplateUpdateDelay(Integer.MAX_VALUE);

        // Apply common template settings
        templateConfig.setObjectWrapper(new DefaultObjectWrapper());
        templateConfig.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        templateConfig.setDefaultEncoding(defaultEncoding);
        templateConfig.setLocalizedLookup(false);

        return new GeneratorMethodEnvironment(templateConfig, filer, elementUtils, logger);
    }

}
