package io.ttyys.algo.text;

import io.ttyys.algo.AlgorithmFactory;
import io.ttyys.core.rpc.protobuf.ClientProxy;
import io.ttyys.core.rpc.protobuf.RPCClientFactory;

import java.lang.reflect.Proxy;

public interface Segmentation extends AlgorithmFactory.Algorithm<Segmentation.SegmentParam, Segmentation.SegmentResult> {
    AlgorithmFactory factory = new SegmentationFactory();

    class SegmentationFactory extends AlgorithmFactory {
        @SuppressWarnings("rawtypes")
        @Override
        public Algorithm create() {
            final ClientProxy rpc = RPCClientFactory.newRPCClientFactory("", 1).createProxy();
            return (Algorithm) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{Segmentation.class}, (proxy, method, args) -> {
                        Class<?> declaring = method.getDeclaringClass();
                        if (declaring.equals(Object.class)) {
                            return method.invoke(this, args);
                        }
                        if (method.getDeclaringClass().equals(Segmentation.class)) {
//                            rpc.send();
                        }
                        throw new IllegalAccessError("unexpect method invoke.");
                    });
        }

        public Segmentation adapt(Algorithm<?, ?> algorithm) {
            return (Segmentation) algorithm;
        }
    }

    class JiebaSegmentation implements Segmentation {
        private final ClientProxy proxy;

        public JiebaSegmentation(ClientProxy proxy) {
            this.proxy = proxy;
        }

        @Override
        public SegmentResult calc(SegmentParam parameter) {
            return null;
        }
    }

    class SegmentParam {}

    class SegmentResult {}
}
