package io.ttyys.core.support.integration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Invoke {
    String serviceInterface();
    String serviceMethod();
}
