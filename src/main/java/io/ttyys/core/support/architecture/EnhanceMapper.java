package io.ttyys.core.support.architecture;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface EnhanceMapper {
    String value() default "";
}
