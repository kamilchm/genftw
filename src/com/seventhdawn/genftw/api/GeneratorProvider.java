package com.seventhdawn.genftw.api;

/**
 * Provider interface for obtaining {@code Generator} instances.
 * <p>
 * Note that each {@code GeneratorProvider} implementation must provide a public no-argument constructor.
 * 
 * @see Generator
 */
public interface GeneratorProvider {

    /**
     * Returns an instance of the given generator class.
     */
    <T> T getInstance(Class<T> generatorClass);

}
