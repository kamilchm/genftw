package com.seventhdawn.genftw.api;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * The context of generator method execution.
 * <p>
 * Generator methods can optionally declare a {@code GeneratorContext} parameter for accessing advanced generator
 * features. Note that a new {@code GeneratorContext} instance will be passed to the given method each time it is
 * invoked by {@code GeneratorProcessor}.
 * 
 * @see Generator
 * @see Generates
 * @see TypeInfo
 */
public interface GeneratorContext {

    /**
     * Sets a custom {@code file} path variable for use with the {@code Generates} annotation.
     * 
     * @param name
     *            Variable name.
     * @param value
     *            Variable value.
     */
    void setFilePathVariable(String name, String value);

    /**
     * Sets a custom {@code using} path variable for use with the {@code Generates} annotation.
     * 
     * @param name
     *            Variable name.
     * @param value
     *            Variable value.
     */
    void setTemplatePathVariable(String name, String value);

    /**
     * Sets a custom {@code file} and {@code using} path variable for use with the {@code Generates} annotation.
     * <p>
     * This is a convenience method equivalent to calling {@link #setFilePathVariable(String, String)
     * setFilePathVariable} and {@link #setTemplatePathVariable(String, String) setTemplatePathVariable} with same
     * parameter values.
     * 
     * @param name
     *            Variable name.
     * @param value
     *            Variable value.
     */
    void setFileAndTemplatePathVariable(String name, String value);

    /**
     * Provides access to the underlying {@code ProcessingEnvironment} of {@code GeneratorProcessor}.
     */
    ProcessingEnvironment getProcessingEnvironment();

    /**
     * Returns the {@code TypeInfo} corresponding to the given type element.
     * 
     * @param type
     *            Type element for which to obtain {@code TypeInfo}.
     */
    TypeInfo getTypeInfo(TypeElement type);

}
