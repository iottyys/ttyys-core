package io.ttyys.camel.component.domain;

import io.ttyys.core.support.camel.ThreadLocalRegistry;
import org.apache.camel.Endpoint;
import org.apache.camel.component.bean.BeanComponent;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultRegistry;
import org.apache.camel.util.PropertiesHelper;

import java.util.Map;

public class DomainComponent extends BeanComponent {


    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        DomainEndpoint endpoint = new DomainEndpoint(uri, this);
        endpoint.setBeanName(remaining);
        endpoint.setScope(getScope());
        setProperties(endpoint, parameters);
        Map<String, Object> options = PropertiesHelper.extractProperties(parameters, "bean.");
        endpoint.setParameters(options);
        return endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        Registry registry = getCamelContext().getRegistry();
        if (registry instanceof DefaultRegistry) {
            ((DefaultRegistry) registry).setFallbackRegistry(new ThreadLocalRegistry());
        }
    }
}
