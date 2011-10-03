package com.seventhdawn.genftw.api;

/**
 * Provides various type information utility methods.
 * 
 * @see GeneratorContext
 */
public interface TypeInfo {

    /**
     * Returns the relative package path of the source file of the given type, e.g. {@code com/myproject}.
     */
    String getPath();

    /**
     * Returns the simple name of the given type, e.g. {@code MyType}.
     */
    String getSimpleName();

    /**
     * Returns the fully qualified (canonical) name of the given type, e.g. {@code com.myproject.MyType}.
     */
    String getFullName();

    /**
     * Returns the package name of the given type, e.g. {@code com.myproject}.
     */
    String getPackageName();

}
