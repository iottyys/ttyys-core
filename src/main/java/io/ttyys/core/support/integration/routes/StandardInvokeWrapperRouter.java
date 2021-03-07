package io.ttyys.core.support.integration.routes;

import io.ttyys.core.support.integration.InvocationProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class StandardInvokeWrapperRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from(InvocationProcessor.POINT)
                .choice()
                    // java参数
                    .when().simple("")
                    .to("") // 入参校验
                    .endChoice()
                    // json参数
                    .when().simple("")
                    .to("") // 入参校验
                    .to("") // 转为java obj
                    .endChoice()
                    // gql参数
                    .when().simple("")
                    .to("") // 入参校验
                    .to("") // 转为java obj
                    .endChoice()
                .end()
                .to("") // 执行接口方法
                .choice()
                    // java返回值
                    .when().simple("")
                    .to("") // 校验返回obj
                    .endChoice()
                    // json返回值
                    .when().simple("")
                    .to("") // 返回值校验
                    .to("") // 转为java obj
                    .endChoice()
                    // gql返回值
                    .when().simple("")
                    .to("") // 返回值校验
                    .to("") // 转为java obj
                    .endChoice()
                .end()
                .log(InvocationProcessor.POINT)
                .end();
    }
}
