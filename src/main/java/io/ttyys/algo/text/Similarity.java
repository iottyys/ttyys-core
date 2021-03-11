//package io.ttyys.algo.text;
//
//import com.github.dozermapper.core.DozerBeanMapperBuilder;
//import com.github.dozermapper.core.Mapper;
//import com.github.dozermapper.core.loader.api.BeanMappingBuilder;
//import com.github.dozermapper.core.loader.api.FieldsMappingOptions;
//import io.ttyys.algo.AlgorithmFactory;
//import io.ttyys.core.rpc.protobuf.ClientProxy;
//
//import java.util.List;
//
//import static com.github.dozermapper.core.loader.api.TypeMappingOptions.*;
//
//public interface Similarity extends AlgorithmFactory.Algorithm<Similarity.SimilarityParam, Similarity.SimilarityResult> {
//    AlgorithmFactory factory = new SimilarityFactory();
//
//    class SimilarityFactory extends AlgorithmFactory {
//        private final ClientProxy proxy;
//        private final Mapper mapper;
//
//        public SimilarityFactory() {
//            super();
//            this.proxy = super.rpcClientFactory.getProxy("protoClassName");
//            BeanMappingBuilder builder = new BeanMappingBuilder() {
//                @Override
//                protected void configure() {
//                    mapping(SimilarityParam.class.getName(),
//                            "protoClassName" + "$" + "paramName", oneWay(), mapId("param"), mapNull(true))
//                            .fields("", "", FieldsMappingOptions.oneWay())
//                            .fields("", "");
//                    mapping("protoClassName" + "$" + "resultClassName",
//                            SimilarityResult.class.getName(), oneWay(), mapId("result"), mapNull(true))
//                            .fields("", "", FieldsMappingOptions.oneWay())
//                            .fields("", "");
//                }
//            };
//            this.mapper = DozerBeanMapperBuilder.create().withMappingBuilder(builder).build();
//        }
//
//        @SuppressWarnings({"rawtypes", "unchecked"})
//        @Override
//        public Algorithm create() {
//            // 构建客户端代理
//            return parameter -> {
//                Object result = this.proxy.service("").send("rpcName", parameter, this.mapper);
//                return this.mapper.map(result, SimilarityResult.class);
//            };
//        }
//
//        @Override
//        public List<Algorithm<?, ?>> utils() {
//            return null;
//        }
//    }
//
//    class SimilarityParam {
//        private String docFilePath;
//        private String corpusFilePath;
//        private String stopWordFile;
//        private String userDict;
//    }
//
//    class SimilarityResult {
//        private String resultFilePath;
//    }
//}
