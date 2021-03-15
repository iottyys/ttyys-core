package io.ttyys.algo.text;

public class SimilarityFactory {
    public Similarity newInstance() {
        return new Similarity() {
            @Override
            public ResultReader similarity(ParamWriter paramWriter) {
                return null;
            }
        };
    }

    static class ParamWriter {}

    static class ResultReader {}

    interface Similarity {
        ResultReader similarity(ParamWriter paramWriter);
    }
}
