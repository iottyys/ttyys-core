package io.ttyys.data.rpcclient;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import io.ttyys.data.rpc.SocketRPCProto;

public class ClientRPCController implements RpcController {
    private boolean failed = false;
    private String error = null;
    private SocketRPCProto.ErrorReason reason = null;

    @Override
    public void reset() {
        this.failed = false;
        this.error = null;
        this.reason = null;
    }

    @Override
    public boolean failed() {
        return this.failed;
    }

    @Override
    public String errorText() {
        return this.error;
    }

    @Override
    public void startCancel() {
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }

    @Override
    public void setFailed(String reason) {
        this.failed = true;
        this.error = reason;
    }

    void setFailed(String error, SocketRPCProto.ErrorReason errorReason) {
        setFailed(error);
        this.reason = errorReason;
    }

    @Override
    public boolean isCanceled() {
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> callback) {
        throw new UnsupportedOperationException(
                "Cannot cancel request in Socket RPC");
    }
}
