package io.ttyys.core.support.camel.routes;

import com.google.common.collect.ImmutableSet;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.BeanComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StandardApplicationServiceRouter extends RouteBuilder {

    @Override
    public void configure() {
        from("direct:test").to("domain:io.ttyys.core.support.camel.routes.BeanInjectionProcessor?injection=testBean").log("test");
//        from("direct:start").to("class:com.snszyk.iiot.lmat.test1?method=test()");
        from("direct:io.ttyys.core.support.StandardApplicationService")
                // todo 判断输入类型，如果为json，使用json-schema-validate校验
                // todo 转换为对象（jsonschema2pojo gradle plugin）
                // todo 如果输入为object，使用bean-validate进行校验
                // todo 执行指定的endpoint
                // todo 判断输出类型，如果为json，则转换为json
                // todo 使用json-schema-validate校验结果json
                // todo 如果输出为object，使用bean-validate进行校验
                .log("direct:io.ttyys.core.support.StandardApplicationService");
//        // todo 异常处理+事务
    }

    public static void main(String[] args) throws Exception {
        Set<String> schemas = ImmutableSet.of("class", "bean");
        String injectRegexPattern = "(-[a-zA-Z]+:.*-)*";
        String vs = "bean";
//        String test = String.format("^%s:.*\\?.*%s.*$", vs, inject);
        Set<String> uriRegexs = schemas.stream().map(v -> String.format("^%s:.*\\?.*method=%s.*$", v, injectRegexPattern)).collect(Collectors.toSet());
        try (CamelContext camel = new DefaultCamelContext()) {
            camel.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    for (String uri : uriRegexs) {
                        interceptSendToEndpoint(uri).process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                System.out.println(exchange);
                            }
                        }).skipSendToOriginalEndpoint();
                    }
//                    from("timer:foo?exchangePattern=InOut")
                    from("direct:start111")
                            .bean(ReturnObj.class)
                            .bean(Test.class, "tcreate")
//                            .routingSlip(simple("class:com.snszyk.iiot.lmat.test1?method=test(),class:com.snszyk.iiot.lmat.test1?method=test()"))
                            .to("class:com.snszyk.iiot.lmat.test1?method=test()")
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    System.out.println(exchange);
                                }
                            });
//                            .toD("bean:testBean?method=-test:tests-test()")
//                            .transform().simple("${body.changeState}")
//                            .log(LoggingLevel.DEBUG, LoggerFactory.getLogger("test"), "test")
//                            .log(simple("${body}").getText());
                }
            });
            camel.start();
            camel.createProducerTemplate().sendBody("direct:start111", null);
            Thread.sleep(10_000);
            camel.stop();
        }
    }

    public static class MyProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            System.out.println(exchange.getIn().getHeaders());
        }
    }

    public static class Test {
        public static ReturnObj create() {
            return new ReturnObj();
        }
        public Test tcreate() {
            return new Test();
        }
    }

    public static class ReturnObj {
        int i = 1;

        public void changeState() {
            this.i = 2;
        }
    }
}
