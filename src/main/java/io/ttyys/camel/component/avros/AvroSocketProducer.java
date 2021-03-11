package io.ttyys.camel.component.avros;

import org.apache.avro.ipc.SaslSocketTransceiver;
import org.apache.avro.ipc.Transceiver;
import org.apache.camel.Endpoint;
import org.apache.camel.component.avro.AvroConfiguration;
import org.apache.camel.component.avro.AvroProducer;

import java.net.InetSocketAddress;

public class AvroSocketProducer extends AvroProducer {
    public AvroSocketProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public Transceiver createTransceiver() throws Exception {
        AvroConfiguration configuration = getEndpoint().getConfiguration();
        return new SaslSocketTransceiver(new InetSocketAddress(configuration.getHost(), configuration.getPort()));
    }
}
