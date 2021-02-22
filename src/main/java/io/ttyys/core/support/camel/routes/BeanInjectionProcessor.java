package io.ttyys.core.support.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class BeanInjectionProcessor {
    public TTTBean process(Exchange exchange) throws Exception {
        System.out.println("前置处理aaa");
        return new TTTBean();
    }
}
