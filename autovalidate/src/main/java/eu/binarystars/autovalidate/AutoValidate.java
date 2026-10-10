package eu.binarystars.autovalidate;


import java.lang.annotation.*;

/**
 * Makes objects validate themselves when they are constructed.
 * <p>
 * Right after an annotated constructor has finished, the new object is checked against its
 * Jakarta Validation constraints. If it is invalid then the constructor throws a
 * {@link jakarta.validation.ConstraintViolationException}, i.e. the object never becomes available.
 * <p>
 * Put the annotation on a class in order to cover all of its constructors, or on a single constructor.
 * <p>
 * The annotation only has an effect if the JVM runs with the AspectJ weaver as Java agent
 * ({@code -javaagent:/path/to/aspectjweaver.jar}). Without it the annotation is silently ignored.
 *
 * @see AutoValidateAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
public @interface AutoValidate {
}
