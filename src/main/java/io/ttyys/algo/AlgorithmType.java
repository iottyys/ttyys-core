package io.ttyys.algo;

public enum AlgorithmType {
    SIMILARITY {
        final String endpoint = "direct:io.ttyys.algo.AlgorithmType.SIMILARITY";

        @Override
        public String avpr() {
            return "avro/similarity.avpr";
        }

        @Override
        public String endpoint() {
            return this.endpoint;
        }
    };

    public abstract String avpr();
    public abstract String endpoint();
}
