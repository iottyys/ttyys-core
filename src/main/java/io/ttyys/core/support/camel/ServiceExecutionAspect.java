package io.ttyys.core.support.camel;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Aspect
@Component
public class ServiceExecutionAspect {

    @SuppressWarnings("FieldCanBeLocal")
    private final String point = "direct:io.ttyys.core.support.StandardApplicationService";

    @EndpointInject(property = "point")
    private ProducerTemplate producer;

    @SuppressWarnings("unused")
    public String getPoint() {
        return this.point;
    }

    @Pointcut("@annotation(io.ttyys.core.support.camel.Execution)")
    private void exec() {}

    @Around("ServiceExecutionAspect.exec() && @annotation(execution)")
    public Object doExec(ProceedingJoinPoint joinPoint, Execution execution) throws Throwable {
        Map<String, Object> headers = ImmutableMap.<String, Object>builder()
//                .put("serviceUri", execution.value())
                .putAll(this.processArgumentType(joinPoint, execution))
                .putAll(this.processReturnType(joinPoint, execution))
                .build();
        return producer.requestBodyAndHeaders(this.handleRequestBody(joinPoint), headers);
    }

    protected Object handleRequestBody(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 1) {
            throw new IllegalArgumentException("support at most one argument");
        }
        return args.length > 0 ? args[0] : new Object();
    }

    protected Map<String, Object> processArgumentType(ProceedingJoinPoint joinPoint, Execution execution) {
        return ImmutableMap.<String, Object>builder()
                .put("inputSchema", execution.input()) // todo 改为json-schema-validator可以识别的url
                .put("marshal", StringUtils.isEmpty(execution.input()))
                .build();
    }

    protected Map<String, Object> processReturnType(ProceedingJoinPoint joinPoint, Execution execution) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return ImmutableMap.<String, Object>builder()
                .put("outputSchema", execution.output()) // todo 改为json-schema-validator可以识别的url
                .put("unmarshal", StringUtils.isEmpty(execution.output()))
                .put("returnType", signature.getReturnType())
                .build();
    }
}
