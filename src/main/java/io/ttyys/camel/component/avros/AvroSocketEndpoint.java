package io.ttyys.camel.component.avros;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.camel.component.avro.AvroEndpoint;

public class AvroSocketEndpoint extends AvroEndpoint {
    public AvroSocketEndpoint(String remaining, AvrosComponent component, AvroConfiguration config) {
        super(remaining, component, config);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new AvroSocketProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("just producer!!!");
    }
}
