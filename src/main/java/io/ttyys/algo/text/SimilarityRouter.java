package io.ttyys.algo.text;

import io.ttyys.algo.AlgorithmType;
import org.apache.avro.Protocol;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SimilarityRouter extends RouteBuilder {

    @Bean
    public Protocol test() throws IOException {
        return Protocol.parse(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(AlgorithmType.SIMILARITY.avpr()));
    }

    @Override
    public void configure() {
        from(AlgorithmType.SIMILARITY.endpoint())
                .to("avro:netty:localhost:22222/send?protocol=#test")
                .log(AlgorithmType.SIMILARITY.endpoint());
    }
}
