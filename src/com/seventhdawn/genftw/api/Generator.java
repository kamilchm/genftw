package com.seventhdawn.genftw.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.ProcessingEnvironment;

import com.seventhdawn.genftw.spi.TemplateEngine;

/**
 * Indicates that an annotated class is a <em>generator</em>.
 * <p>
 * Generators are used to produce arbitrary files using a specific template engine. Generators are usually POJOs, with
 * methods interacting with given template engine in order to produce relevant output files. Generators are invoked by
 * {@code GeneratorProcessor} automatically whenever a tool that supports JSR-269 (Pluggable Annotation Processing API)
 * is executed. It is often convenient to use Java compiler's native support of this API for processing generators.
 * <p>
 * 
 * <h3>Hello, Generator World!</h3>
 * 
 * Below is a simple generator that demonstrates the basic concepts.
 * 
 * <pre>
 * &#064;Generator(templateEngine = VelocityEngine.class)
 * public class MyGenerator {
 * 
 *     &#064;ForEachType(@TypeMatchCriteria(annotatedWith = FooAnnotation.class))
 *     &#064;Generates(file = &quot;{typePath}/{typeSimpleName}.txt&quot;, using = &quot;FooTemplate.vm&quot;)
 *     public void generateFoo(TypeElement type, VelocityContext vCtx) {
 *         // invoked for each type annotated with FooAnnotation
 *         vCtx.put(&quot;templateVariable&quot;, &quot;someValue&quot;);
 *     }
 * 
 *     &#064;ForAllTypes(@TypeMatchCriteria(annotatedWith = BarAnnotation.class))
 *     &#064;Generates(file = &quot;com/myproject/Bar.txt&quot;, using = &quot;BarTemplate.vm&quot;)
 *     public void generateBar(TypeElement[] types, VelocityContext vCtx) {
 *         // invoked for all types annotated with BarAnnotation
 *         vCtx.put(&quot;templateVariable&quot;, &quot;someValue&quot;);
 *     }
 * 
 *     &#064;Generates(file = &quot;com/myproject/Qux.txt&quot;, using = &quot;QuxTemplate.vm&quot;)
 *     public void generateQux(VelocityContext vCtx) {
 *         // invoked once per generator processing (no type matching)
 *         vCtx.put(&quot;templateVariable&quot;, &quot;someValue&quot;);
 *     }
 * 
 * }
 * </pre>
 * 
 * Each generator uses a specific {@link TemplateEngine} for producing its output files. A {@code TemplateEngine}
 * implementation has two main responsibilities:
 * <p>
 * <ul>
 * <li>resolve any extra parameters (e.g. {@code VelocityContext} in the example above), which are declared by generator
 * methods
 * <li>generate an output file from the template, using any extra parameters that were previously passed to generator
 * methods
 * </ul>
 * <p>
 * Normally, each generator method annotated with {@link Generates} produces one output file. When the annotated method
 * fails (e.g. throws an exception), no file is produced. Note that the order of generator method invocation is
 * undefined.
 * 
 * <h3>Type matching</h3>
 * 
 * Generators usually produce their outputs based on type information. For this purpose, following use cases are
 * supported:
 * <p>
 * <ul>
 * <li>generate an output file for each type that matches given criteria
 * <li>generate an output file for all types that match given criteria
 * </ul>
 * 
 * <h4>{@code ForEachType} annotation</h4>
 * 
 * Use {@link ForEachType} if you want to have a file produced for each type that matches given criteria. For example:
 * 
 * <pre>
 * &#064;ForEachType(@TypeMatchCriteria(annotatedWith = FooAnnotation.class))
 * &#064;Generates(file = &quot;{typePath}/{typeSimpleName}.txt&quot;, using = &quot;FooTemplate.vm&quot;)
 * public void generateFoo(TypeElement type, VelocityContext vCtx) {
 *     FooAnnotation annotation = type.getAnnotation(FooAnnotation.class);
 *     // populate VelocityContext instance
 * }
 * </pre>
 * 
 * {@code ForEachType} allows you to declare a {@code TypeElement} parameter, corresponding to the type being matched,
 * in your generator method. Note that this declaration is optional.
 * <p>
 * The above {@code generateFoo} method will be invoked for each type annotated with {@code FooAnnotation}. In this
 * example, {@code typePath} and {@code typeSimpleName} are special built-in path variables, provided for generator
 * methods that use {@code ForEachType}.
 * 
 * <h4>{@code ForAllTypes} annotation</h4>
 * 
 * Use {@link ForAllTypes} if you want to have a file produced for all types that match given criteria. For example:
 * 
 * <pre>
 * &#064;ForAllTypes(@TypeMatchCriteria(annotatedWith = BarAnnotation.class))
 * &#064;Generates(file = &quot;com/myproject/Bar.txt&quot;, using = &quot;BarTemplate.vm&quot;)
 * public void generateBar(TypeElement[] types, VelocityContext vCtx) {
 *     TypeElement type = types[0];
 *     BarAnnotation annotation = type.getAnnotation(BarAnnotation.class);
 *     // populate VelocityContext instance
 * }
 * </pre>
 * 
 * {@code ForAllTypes} allows you to declare a {@code TypeElement} array parameter, corresponding to all types being
 * matched, in your generator method. Note that this declaration is optional. When invoked, the {@code TypeElement}
 * array is guaranteed to be non-empty.
 * <p>
 * The above {@code ForAllTypes} method will be invoked for all types annotated with {@code BarAnnotation}.
 * 
 * <h4>Corner cases</h4>
 * 
 * It is illegal to use more than one type matching annotation on a generator method. When no type matching annotation
 * is specified, the given generator method will be invoked once per generator processing.
 * 
 * <h3>Output control</h3>
 * 
 * There are two possible return types a generator method can have:
 * <p>
 * <ul>
 * <li>{@code boolean}, in which case the output is produced when the method returns {@code true}
 * <li>{@code void}, in which case the output is always produced, unless the method throws an exception (equivalent to
 * returning {@code true})
 * </ul>
 * <p>
 * For example:
 * 
 * <pre>
 * &#064;Generates(file = &quot;com/myproject/Qux.txt&quot;, using = &quot;QuxTemplate.vm&quot;)
 * public boolean generateQux(VelocityContext vCtx) {
 *     if (shouldSkipFile()) {
 *         return false;
 *     }
 * 
 *     // populate VelocityContext instance
 *     return true;
 * }
 * </pre>
 * 
 * <h3>Using {@code GeneratorContext}</h3>
 * 
 * Generator methods can optionally declare a {@link GeneratorContext} parameter. {@code GeneratorContext} represents
 * the context of generator method execution and provides access to advanced generator features, such as:
 * <p>
 * <ul>
 * <li>ability to set custom {@code file} and {@code using} path variables for use with the {@code Generates} annotation
 * <li>access to the underlying {@link ProcessingEnvironment} of {@code GeneratorProcessor}
 * <li>easy way to get common type information using {@link TypeInfo}
 * </ul>
 * <p>
 * For example:
 * 
 * <pre>
 * &#064;ForEachType(@TypeMatchCriteria(annotatedWith = FooAnnotation.class))
 * &#064;Generates(file = &quot;{typePath}/{typeSimpleName}.{fileExtension}&quot;, using = &quot;FooTemplate{templateVersion}.vm&quot;)
 * public void generateFoo(TypeElement type, GeneratorContext gCtx, VelocityContext vCtx) {
 *     // &quot;typePath&quot; and &quot;typeSimpleName&quot; are resolved automatically
 *     gCtx.setFilePathVariable(&quot;fileExtension&quot;, &quot;txt&quot;);
 *     gCtx.setTemplatePathVariable(&quot;templateVersion&quot;, &quot;Text&quot;);
 *     // write some log message
 *     gCtx.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.NOTE, &quot;A log message&quot;);
 *     // get common type information
 *     String typeFullName = gCtx.getTypeInfo(type).getFullName();
 *     // populate VelocityContext instance
 * }
 * </pre>
 * 
 * <h3>Providing generator instances</h3>
 * 
 * Normally, each generator must be a non-abstract class with a public no-argument constructor. Use the
 * {@linkplain #provider} option if you want to lift these restrictions on your generator classes. For example:
 * 
 * <pre>
 * &#064;Generator(templateEngine = VelocityEngine.class, provider = MyGeneratorProvider.class)
 * public class MyGenerator {
 * 
 *     // generator methods omitted
 * 
 * }
 * </pre>
 * 
 * <h3>Life-cycle hooks</h3>
 * 
 * The framework provides you with following hooks regarding the generator life-cycle:
 * <p>
 * <ul>
 * <li>{@link BeforeEach} methods, invoked before processing each generator method
 * <li>{@link AfterEach} methods, invoked after processing each generator method
 * <li>{@link BeforeAll} methods, invoked before processing all generator methods
 * <li>{@link AfterAll} methods, invoked after processing all generator methods
 * </ul>
 * <p>
 * Methods using life-cycle annotations must return {@code void} and have no arguments. For example:
 * 
 * <pre>
 * &#064;Generator(templateEngine = VelocityEngine.class)
 * public class MyGenerator {
 * 
 *     // annotations omitted
 *     public void generateFoo(TypeElement type, VelocityContext vCtx) {
 *         // populate VelocityContext instance
 *     }
 * 
 *     // annotations omitted
 *     public void generateBar(TypeElement[] types, VelocityContext vCtx) {
 *         // populate VelocityContext instance
 *     }
 * 
 *     &#064;BeforeEach
 *     public void localInit() {
 *         // called before each generateFoo and generateBar invocation
 *     }
 * 
 *     &#064;BeforeAll
 *     public void globalInit() {
 *         // called once, before processing generateFoo and generateBar
 *     }
 * 
 * }
 * </pre>
 * 
 * When a generator life-cycle method fails (e.g. throws an exception), processing of the corresponding generator will
 * be halted. Note that the order of method invocation for a given life-cycle annotation is undefined.
 * 
 * @see Generates
 * @see ForEachType
 * @see ForAllTypes
 * @see TypeMatchCriteria
 * @see GeneratorContext
 * @see GeneratorProvider
 * @see BeforeEach
 * @see AfterEach
 * @see BeforeAll
 * @see AfterAll
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Generator {

    /**
     * {@code TemplateEngine} implementation to use for producing output files.
     */
    Class<? extends TemplateEngine> templateEngine();

    /**
     * An optional {@code GeneratorProvider} implementation used by {@code GeneratorProcessor} to obtain the generator
     * instance.
     * <p>
     * In this case, the default {@code GeneratorProvider.class} value is a null object, used to bypass instance
     * provider functionality.
     */
    Class<? extends GeneratorProvider> provider() default GeneratorProvider.class;

}
