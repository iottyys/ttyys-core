package io.ttyys.core.support.camel;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class InvocationProcessor {

    public static final String POINT = "direct:io.ttyys.core.support.camel.routes.StandardInvokeWrapperRouter";

    @EndpointInject(property = "point")
    private ProducerTemplate producer;

    public String getPoint() {
        return POINT;
    }

    @Pointcut("@annotation(io.ttyys.core.support.camel.Invoke)")
    private void invoke() {}

    @Around("InvocationProcessor.invoke() && @annotation(invoke)")
    public Object doInvoke(ProceedingJoinPoint joinPoint, Invoke invoke) {
        Object body = this.handleRequestBody(joinPoint);
        Map<String, Object> headers = ImmutableMap.<String, Object>builder()
                .put("serviceInterface", invoke.serviceInterface())
                .put("serviceMethod", invoke.serviceMethod())
                .put("convertFile", invoke.convertFile())
//                .putAll(this.processArgumentType(joinPoint, invoke))
//                .putAll(this.processReturnType(joinPoint, invoke))
                .build();
        return this.producer.requestBodyAndHeaders(body, headers);
    }

    protected Object handleRequestBody(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1) {
            throw new IllegalArgumentException("support at most one argument");
        }
        return args.length > 0 ? args[0] != null ? args[0] : new Object() : new Object();
    }
}
