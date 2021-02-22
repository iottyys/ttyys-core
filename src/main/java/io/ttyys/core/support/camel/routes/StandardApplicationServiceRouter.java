package io.ttyys.core.support.camel.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class StandardApplicationServiceRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:start1").log("test logic1");
        from("direct:start2").log("test logic2");
        from("direct:io.ttyys.core.support.StandardApplicationService")
                .choice()
                    .when(simple("${header.isJsonInput}", boolean.class))
                        .toD("json-validator:${header.inputSchema}")
                        // todo 转换为对象（jsonschema2pojo gradle plugin）
                        .unmarshal().json()
                        .endChoice()
                    .when(simple("${header.isJavaInput}", boolean.class))
                        .toD("bean-validator:${header.inputSchema}")
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
                        .toD("bean-validator:${header.outputSchema}")
                        .endChoice()
                .end()
                .log("direct:io.ttyys.core.support.StandardApplicationService")
                .end();
//        // todo 异常处理+事务
    }
}
