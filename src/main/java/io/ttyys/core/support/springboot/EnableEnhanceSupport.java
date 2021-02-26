package io.ttyys.core.support.springboot;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({SpringBootAutoConfiguration.class,
        ClassPathEnhanceServiceScannerRegistrar.class,
        ClassPathEnhanceMapperScannerRegistrar.class})
public @interface EnableEnhanceSupport {
    String[] servicePackages() default {};
    String[] mapperPackages() default {};
}
