package io.ttyys.core.support.springboot;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({SpringBootAutoConfiguration.class, ClassPathApplicationServiceScannerRegistrar.class})
public @interface EnableTYSupport {
    @AliasFor("servicePackages")
    String[] value() default {};
    @AliasFor("value")
    String[] servicePackages() default {};
}
