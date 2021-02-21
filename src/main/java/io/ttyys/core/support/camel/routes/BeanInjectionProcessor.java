package io.ttyys.core.support.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BeanInjectionProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("前置处理aaa");
    }
}
