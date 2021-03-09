package io.ttyys.algo;

import io.ttyys.algo.text.Similarity;

public enum AlgorithmType {
    SIMILARITY {
        @Override
        public AlgorithmFactory factory() {
            return Similarity.factory;
        }
    };

    public abstract AlgorithmFactory factory();
}
