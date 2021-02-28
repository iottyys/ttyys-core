package io.ttyys.core.support.springboot;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({MybatisPlusAutoConfiguration.AutoConfiguredMapperScannerRegistrar.class,
        SpringBootAutoConfiguration.class,
        ClassPathEnhanceServiceScannerRegistrar.class,
        ClassPathEnhanceMapperScannerRegistrar.class})
public @interface EnableEnhanceSupport {
    String[] servicePackages() default {};
    String[] mapperPackages() default {};
}
