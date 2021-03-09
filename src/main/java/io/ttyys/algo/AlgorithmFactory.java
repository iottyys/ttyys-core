package io.ttyys.algo;

import io.ttyys.core.rpc.protobuf.RPCClientFactory;

import java.io.IOException;
import java.util.List;

public abstract class AlgorithmFactory {
    protected final RPCClientFactory rpcClientFactory = RPCClientFactory
            .newRPCClientFactory("local", 22222);

    private static final String ALGO_PROTO_LOCATION = "classpath*:**/protobuf/algo/*.proto";

    public AlgorithmFactory() {
        try {
            this.rpcClientFactory.createProxies(new String[] { ALGO_PROTO_LOCATION });
        } catch (Exception e) {
            throw new IllegalStateException("could not create factory. ", e);
        }
    }

    public static AlgorithmFactory newInstance(AlgorithmType algo) {
        return algo.factory();
    }

    public abstract <P, R> Algorithm<P, R> create();
    public abstract List<Algorithm<?, ?>> utils();

    public interface Algorithm<P, R> {
        R calc(P parameter);
    }
}
