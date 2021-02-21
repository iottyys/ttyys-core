package io.ttyys.camel.component.domain;

import org.apache.camel.BeanScope;
import org.apache.camel.Component;
import org.apache.camel.component.bean.*;
import org.apache.camel.spi.UriParam;

public class DomainEndpoint extends BeanEndpoint {
    private transient BeanProcessor processor;

    @UriParam
    private String injection;

    public DomainEndpoint(String uri, Component component) {
        super(uri, component);
    }

    @Override
    protected void doInit() throws Exception {
        super.doInit();

        if (processor == null) {
            BeanHolder holder = getBeanHolder();
            if (holder == null) {
                ParameterMappingStrategy strategy
                        = ParameterMappingStrategyHelper.createParameterMappingStrategy(getCamelContext());
                DomainComponent bean = getCamelContext().getComponent("domain", DomainComponent.class);
                RegistryBean registryBean
                        = new RegistryBean(getCamelContext(), getBeanName(), strategy, bean);
//                DomainRegistryBean domainRegistryBean =
                if (getScope() == BeanScope.Singleton) {
                    // if singleton then create a cached holder that use the same singleton instance
                    holder = registryBean.createCacheHolder();
                } else {
                    holder = registryBean;
                }
            }
            if (getScope() == BeanScope.Request) {
                // wrap in registry scoped
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

    public void setInjection(String injection) {
        this.injection = injection;
    }
}
