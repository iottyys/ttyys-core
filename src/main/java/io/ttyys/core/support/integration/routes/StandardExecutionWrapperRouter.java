package io.ttyys.core.support.integration.routes;

import io.ttyys.core.support.integration.ExecutionProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class StandardExecutionWrapperRouter extends RouteBuilder {
    @Override
    public void configure() {
        from(ExecutionProcessor.POINT)
                .choice()
                    .when(simple("${header.isJsonInput}", boolean.class))
                        .toD("json-validator:${header.inputSchema}")
                        // todo 转换为对象（jsonschema2pojo gradle plugin）
                        .unmarshal().json()
                        .endChoice()
                    .when(simple("${header.isJavaInput}", boolean.class))
                        .toD("bean-validator:abc") // todo tengwang 修复
                        .endChoice()
                .end()
                .routingSlip(simple("${header.serviceUris}", String.class))
                .choice()
                    .when(simple("${header.isJsonOutput}", boolean.class))
                        // todo 如果为json，则转换为json
                        .marshal().json()
                        .toD("json-validator:${header.outputSchema}")
                        .endChoice()
                    .when(simple("${header.isJavaOutput}", boolean.class))
                        .toD("bean-validator:def") // todo tengwang 修复
                        .endChoice()
                .end()
                .log(ExecutionProcessor.POINT)
                .end();
//        // todo 异常处理+事务
    }
}
