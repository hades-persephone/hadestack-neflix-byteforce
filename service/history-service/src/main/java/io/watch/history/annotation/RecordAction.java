package io.watch.history.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically record actions when methods are executed
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordAction {

    /**
     * The type of entity being acted upon
     * Can be a static value or a SpEL expression to extract from method arguments
     */
    String entityType();

    /**
     * The ID of the entity being acted upon
     * Can be a static value or a SpEL expression to extract from method arguments
     */
    String entityId();

    /**
     * The type of action being performed
     */
    String actionType();

    /**
     * Additional details to record about the action
     * Format: {"key1": "value1", "key2": "value2"} or SpEL expressions
     */
    String[] details() default {};

    /**
     * Whether to record the action asynchronously (default: true)
     */
    boolean async() default true;

    /**
     * SpEL expression to determine if the action should be recorded
     * Default is to always record
     */
    String condition() default "true";
}