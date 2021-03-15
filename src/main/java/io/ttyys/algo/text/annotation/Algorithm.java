package io.ttyys.algo.text.annotation;

import io.ttyys.algo.AlgorithmType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface Algorithm {
    AlgorithmType value();
}
