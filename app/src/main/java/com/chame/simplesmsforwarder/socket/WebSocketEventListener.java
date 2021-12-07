package com.chame.simplesmsforwarder.socket;

import com.neovisionaries.ws.client.*;

import java.util.List;
import java.util.Map;


public class WebSocketEventListener extends WebSocketAdapter {
    private OnSocketSuccessListener successListener;
    private OnSocketFailureListener failureListener;
    private OnSocketMessageListener messageListener;

    public WebSocketEventListener(OnSocketSuccessListener successListener, OnSocketFailureListener failureListener) {
        this.successListener = successListener;
        this.failureListener = failureListener;
    }

    public WebSocketEventListener(OnSocketSuccessListener successListener, OnSocketFailureListener failureListener,
                                  OnSocketMessageListener messageListener) {
        this.successListener = successListener;
        this.failureListener = failureListener;
        this.messageListener = messageListener;
    }

    // region Listeners
    public void setSuccessListener(OnSocketSuccessListener successListener) {
        this.successListener = successListener;
    }

    public void setFailureListener(OnSocketFailureListener failureListener) {
        this.failureListener = failureListener;
    }

    public void setMessageListener(OnSocketMessageListener messageListener) {
        this.messageListener = messageListener;
    }
    // endregion

    // region Call Listeners
    private void doFailure(FailureCode f) {
        if (failureListener != null) failureListener.onConnectionFailure(f);
    }

    private void doMessage(String msg) {
        if (messageListener != null) messageListener.onMessage(msg);
    }

    private void doSuccess() {
        if (successListener != null) successListener.onConnectionSuccess();
    }
    // endregion

    // region Callbacks
    @Override
    public void onTextMessage(WebSocket websocket, String msg) throws Exception {
        if (msg.equals("Authenticated")) {
            doSuccess();
        } else if (msg.equals("Unauthenticated")) {
            doFailure(FailureCode.AuthError);
        } else {
            doMessage(msg);
        }
    }

    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        doSuccess();
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        doFailure(FailureCode.ConnectionError);
    }

    @Override
    public void onDisconnected(WebSocket websocket,
                               WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                               boolean closedByServer) throws Exception {
        doFailure(FailureCode.ConnectionError);
    }
    //endregion

    // region Interfaces

    public enum FailureCode{
        InvalidUri,
        ConnectionError,
        AuthError,
        GenericError
    }

    public interface OnSocketFailureListener {
        void onConnectionFailure(FailureCode f);
    }

    public interface OnSocketSuccessListener {
        void onConnectionSuccess();
    }

    public interface OnSocketMessageListener {
        void onMessage(String msg);
    }
    // endregion
}
