package io.ttyys.camel.component.avros;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.component.avro.*;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.util.URISupport;

import java.net.URI;
import java.util.Map;

@Component("avros")
public class AvrosComponent extends DefaultComponent {

    @Metadata(label = "advanced")
    private AvroConfiguration configuration;

    public AvrosComponent() {
    }

    public AvrosComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        AvroConfiguration config;
        if (getConfiguration() != null) {
            config = getConfiguration().copy();
        } else {
            config = new AvroConfiguration();
        }

        URI endpointUri = new URI(URISupport.normalizeUri(remaining));
        config.parseURI(endpointUri);

        Endpoint answer;
        if (AvroConstants.AVRO_NETTY_TRANSPORT.equals(endpointUri.getScheme())) {
            answer = new AvroSocketEndpoint(remaining, this, config);
        } else if (AvroConstants.AVRO_HTTP_TRANSPORT.equals(endpointUri.getScheme())) {
            answer = new AvroHttpEndpoint(remaining, this, config);
        } else {
            throw new IllegalArgumentException("Unknown avro scheme. Should use either netty or http.");
        }
        setProperties(answer, parameters);
        return answer;
    }

    public AvroConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AvroConfiguration configuration) {
        this.configuration = configuration;
    }
}
