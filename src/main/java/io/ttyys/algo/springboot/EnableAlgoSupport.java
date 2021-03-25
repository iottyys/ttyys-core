package io.ttyys.algo.springboot;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({SpringBootAutoConfiguration.class})
public @interface EnableAlgoSupport {
}
