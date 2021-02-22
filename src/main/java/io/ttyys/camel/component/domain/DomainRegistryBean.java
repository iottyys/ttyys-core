package io.ttyys.camel.component.domain;

import org.apache.camel.CamelContext;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.component.bean.*;

public class DomainRegistryBean extends RegistryBean {
    public DomainRegistryBean(CamelContext context, String name,
                              ParameterMappingStrategy parameterMappingStrategy,
                              BeanComponent beanComponent) {
        super(context, name, parameterMappingStrategy, beanComponent);
    }

    @Override
    public String toString() {
        return "domain: " + getName();
    }

    @Override
    public ConstantBeanHolder createCacheHolder() {
        try {
            Object bean = getBean(null);
            BeanInfo info = createBeanInfo(bean);
            return new ConstantBeanHolder(bean, info);
        } catch (NoSuchBeanException e) {
            // todo check outputs of RouteDefinition, injection={beanName} must appear before this
            return null;
        }
    }
}
