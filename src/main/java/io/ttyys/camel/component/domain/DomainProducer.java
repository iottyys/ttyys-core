package io.ttyys.camel.component.domain;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.component.bean.BeanEndpoint;
import org.apache.camel.component.bean.BeanProcessor;
import org.apache.camel.component.bean.BeanProducer;
import org.springframework.util.StringUtils;

public class DomainProducer extends BeanProducer {

    private String injection;
    private BeanProcessor processor;

    public DomainProducer(BeanEndpoint endpoint, BeanProcessor processor) {
        super(endpoint, processor);
        if (endpoint instanceof DomainEndpoint) {
            DomainEndpoint domainEndpoint = (DomainEndpoint) endpoint;
            this.injection = domainEndpoint.getInjection();
            this.processor = domainEndpoint.getProcessor();
        }
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        boolean retVal = super.process(exchange, callback);
        if (StringUtils.hasText(this.injection)) {
            return retVal;
        }
        this.injectBean(exchange);
        return retVal;
    }

    protected void injectBean(Exchange exchange) {
        Object bean = exchange.getMessage().getBody();
        if (bean == null) {
            bean = processor.getBean();
        }
        exchange.getContext().getRegistry().bind(this.injection, bean);
        exchange.getMessage().setBody(null);
    }
}
