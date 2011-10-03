package com.seventhdawn.genftw.spi;

import java.io.PrintWriter;

import com.seventhdawn.genftw.api.Generator;

/**
 * Template engine service provider interface.
 * <p>
 * Each {@link Generator} uses a specific {@code TemplateEngine} for producing its output files. A
 * {@code TemplateEngine} implementation has two main responsibilities:
 * <p>
 * <ul>
 * <li>{@linkplain #resolveMethodParam(Class) resolve} any extra parameters, which are declared by generator methods
 * <li>{@linkplain #generate(String, PrintWriter, Object[]) generate} an output file from the template, using any extra
 * parameters that were previously passed to generator methods
 * </ul>
 * <p>
 * When a generator method declares unsupported parameter types (types not supported by the framework nor by the
 * {@code TemplateEngine} implementation), processing for such method will be skipped.
 * <p>
 * Note that each {@code TemplateEngine} implementation must provide a public no-argument constructor.
 * 
 * @see Generator
 */
public interface TemplateEngine {

    /**
     * Returns extra method parameter types supported by this template engine.
     * <p>
     * Returning {@code null} is equivalent to returning an empty array.
     */
    Class<?>[] getSupportedMethodParamTypes();

    /**
     * Returns {@code true} if the given method parameter type is mandatory, or {@code false} if it is optional.
     * <p>
     * Each {@code Generator} using this template engine must have its generator methods declare all mandatory
     * parameters. When a generator method does not declare all mandatory parameters, processing for such method will be
     * skipped.
     * <p>
     * This method can be called only for {@linkplain #getSupportedMethodParamTypes supported parameter types}.
     * 
     * @param paramClass
     *            Supported method parameter type.
     */
    boolean isMandatoryMethodParam(Class<?> paramClass);

    /**
     * Resolves an instance of the given method parameter type.
     * <p>
     * This method can be called only for {@linkplain #getSupportedMethodParamTypes supported parameter types}.
     * 
     * @param paramClass
     *            Supported method parameter type.
     */
    <T> T resolveMethodParam(Class<T> paramClass);

    /**
     * Initialization callback, called by {@code GeneratorProcessor} before a template engine is put to service.
     */
    void init();

    /**
     * Tear down callback, called by {@code GeneratorProcessor} before a template engine is removed from service.
     */
    void destroy();

    /**
     * Generates an output file from a template, using any extra parameters that were previously passed to a generator
     * method.
     * 
     * @param templateFile
     *            Path to template file.
     * @param outputWriter
     *            Output file writer.
     * @param methodParams
     *            Extra parameters from a generator method.
     * 
     * @throws Exception
     *             In case of any errors.
     */
    void generate(String templateFile, PrintWriter outputWriter, Object[] methodParams) throws Exception;

}
