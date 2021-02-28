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
public class ExecutionProcessor {

    public static final String POINT = "direct:io.ttyys.core.support.camel.routes.StandardExecutionWrapperRouter";


    @SuppressWarnings("unused")
    @EndpointInject(property = "point")
    private ProducerTemplate producer;

    @SuppressWarnings("unused")
    public String getPoint() {
        return POINT;
    }

    @Pointcut("@annotation(io.ttyys.core.support.camel.Execution)")
    private void exec() {}

    @SuppressWarnings("RedundantThrows")
    @Around("ExecutionProcessor.exec() && @annotation(execution)")
    public Object doExec(ProceedingJoinPoint joinPoint, Execution execution) throws Throwable {
        Object body = this.handleRequestBody(joinPoint);
        Map<String, Object> headers = ImmutableMap.<String, Object>builder()
                .put("serviceUris", execution.serviceEndpoints())
                .putAll(this.processArgumentType(joinPoint, execution))
                .putAll(this.processReturnType(joinPoint, execution))
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

    protected Map<String, Object> processArgumentType(ProceedingJoinPoint joinPoint, Execution execution) {
        if (execution.input().isJson() && StringUtils.hasText(execution.input().schema())) {
            throw new IllegalArgumentException("Parameter type of interface belong to Json must have a schema file. ");
        }
        String paramType = joinPoint.getArgs()[0].getClass().getName();
        if (execution.input().isJava() && !StringUtils.hasText(execution.input().schema())) {
            paramType = execution.input().schema();
        }
        return ImmutableMap.<String, Object>builder()
                .put("isJsonInput", execution.input().isJson())
                .put("isJavaInput", execution.input().isJava())
                .put("inputSchema", paramType)
                .build();
    }

    protected Map<String, Object> processReturnType(ProceedingJoinPoint joinPoint, Execution execution) {
        if (execution.output().isJson() && StringUtils.hasText(execution.output().schema())) {
            throw new IllegalArgumentException("Return type of interface belong to Json must have a schema file. ");
        }
        String returnType = ((MethodSignature) joinPoint.getSignature()).getReturnType().getName();
        if (execution.output().isJava() && !StringUtils.hasText(execution.output().schema())) {
            returnType = execution.output().schema();
        }
        return ImmutableMap.<String, Object>builder()
                .put("isJsonOutput", execution.output().isJson())
                .put("isJavaOutput", execution.output().isJava())
                .put("outputSchema", returnType)
                .build();
    }
}
