package io.ttyys.algo;

//import io.ttyys.algo.text.Similarity;

public enum AlgorithmType {
    SIMILARITY {
        final String endpoint = "direct:io.ttyys.algo.AlgorithmType.SIMILARITY";

        @Override
        public AlgorithmFactory factory() {
//            return Similarity.factory;
            return null;
        }

        @Override
        public String avpr() {
            return "avro/test.avpr";
        }

        @Override
        public String endpoint() {
            return this.endpoint;
        }
    };

    public abstract AlgorithmFactory factory();

    public abstract String avpr();
    public abstract String endpoint();
}
