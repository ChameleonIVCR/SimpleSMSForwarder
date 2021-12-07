package com.chame.simplesmsforwarder.socket;

import android.net.Uri;
import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WebSocketClient {
    private WebSocket wSocket = null;
    private SocketClient.OnSocketFailureListener failureListener;

    public WebSocketClient() {

    }

    public void connect(String ip, String port, String token) {
        String root = String.format("%s:%s", ip, port);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("ws").encodedAuthority(root);
        String uri = builder.build().toString();

        try {
            wSocket = new WebSocketFactory().createSocket(uri);
        } catch (IOException e) {
            failureListener.onConnectionFailure(SocketClient.FailureCode.InvalidUri);
            return;
        }

        wSocket.addListener(new WebSocketAdapter() {
            @Override
            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                // Received a text message.
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {

            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            }

            @Override
            public void onDisconnected(WebSocket websocket,
                                       WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                       boolean closedByServer) throws Exception {

            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

            }
        });
    }
}
