package io.ttyys.core.support.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceInvokeAspect {

    private final String point = "direct:io.ttyys.core.support.StandardApplicationServiceInvoke";

    @EndpointInject(property = "point")
    private ProducerTemplate producer;

    public String getPoint() {
        return this.point;
    }

    @Pointcut("@annotation(io.ttyys.core.support.camel.Invoke)")
    private void invoke() {}

    @Around("ServiceInvokeAspect.invoke() && @annotation(invoke)")
    public Object doInvoke(ProceedingJoinPoint joinPoint, Invoke invoke) {
        System.out.println("使用camel代理执行内置路由，validate校验入参对象符合注解要求并用jslt转换为service需要的参数");
        System.out.println("获取指定的service接口的对象，执行");
        return null;
    }
}
