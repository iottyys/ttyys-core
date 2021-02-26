package io.ttyys.core.support.camel.routes;

import io.ttyys.core.support.camel.InvocationProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class StandardInvokeWrapperRouter extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from(InvocationProcessor.POINT)
                .log(InvocationProcessor.POINT);
    }
}
