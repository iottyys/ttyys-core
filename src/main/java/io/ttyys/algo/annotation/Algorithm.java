package io.ttyys.algo.annotation;

import io.ttyys.algo.AlgorithmType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Documented
public @interface Algorithm {
    AlgorithmType value();
}
