package io.ttyys.core.support.camel.routes;

import io.ttyys.core.support.camel.InvocationProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class StandardInvokeWrapperRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from(InvocationProcessor.POINT)
                // 从invoke注解上获取Dozer的转换文件

                // 拿到入参,先to一下validator
                // 调用Dozer转换为请求对象
                // 根据invoke注解配置的service及method拼装to调用
                // 调用Dozer进行出模型转换
                // 调用返回结果对象,to一下validator
                // 返回
                .log(InvocationProcessor.POINT);
    }
}
