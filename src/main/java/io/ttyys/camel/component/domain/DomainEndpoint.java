package io.ttyys.camel.component.domain;

import org.apache.camel.BeanScope;
import org.apache.camel.Component;
import org.apache.camel.Producer;
import org.apache.camel.component.bean.*;
import org.apache.camel.spi.UriParam;

public class DomainEndpoint extends BeanEndpoint {
    private transient BeanProcessor processor;

    @UriParam
    private String injection = "";

    public DomainEndpoint(String uri, Component component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() {
        return new DomainProducer(this, processor);
    }

    public BeanProcessor getProcessor() {
        return processor;
    }

    @Override
    protected void doInit() {
        if (processor == null) {
            BeanHolder holder = getBeanHolder();
            if (holder == null) {
                ParameterMappingStrategy strategy
                        = ParameterMappingStrategyHelper.createParameterMappingStrategy(getCamelContext());
                DomainComponent domain = getCamelContext().getComponent("domain", DomainComponent.class);
                DomainRegistryBean registryBean
                        = new DomainRegistryBean(getCamelContext(), getBeanName(), strategy, domain);
                if (getScope() == BeanScope.Singleton) {
                    holder = registryBean.createCacheHolder();
                } else {
                    holder = registryBean;
                }
                if (holder == null) {
                    holder = registryBean;
                }
            }
            if (getScope() == BeanScope.Request) {
                holder = new RequestBeanHolder(holder);
            }
            processor = new BeanProcessor(holder);
            if (getMethod() != null) {
                processor.setMethod(getMethod());
            }
            processor.setScope(getScope());
            if (getParameters() != null) {
                holder.setOptions(getParameters());
            }
        }
    }

    @Override
    protected String createEndpointUri() {
        return "domain:" + getBeanName() + (getMethod() != null ? "?method=" + getMethod() : "");
    }

    public String getInjection() {
        return injection;
    }

    @SuppressWarnings("unused")
    public void setInjection(String injection) {
        this.injection = injection;
    }
}
