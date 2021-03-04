package io.ttyys.data.rpcclient;

import com.google.protobuf.*;

public abstract class ClientChannel implements BlockingRpcChannel {
    public static ClientChannel defaultChannel(
            RPCClientFactory connectionFactory) {
        return new DefaultClientChannel(connectionFactory);
    }
}
