package io.ttyys.algo;

public abstract class AlgorithmFactory {
    public abstract Algorithm create();

    public interface Algorithm<P, R> {
        R calc(P parameter);
    }
}
