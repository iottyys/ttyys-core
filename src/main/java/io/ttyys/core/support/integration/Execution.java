package io.ttyys.core.support.integration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Execution {
    String serviceEndpoints();
    InterfaceType input() default InterfaceType.JavaObj;
    InterfaceType output() default InterfaceType.JavaObj;
}
