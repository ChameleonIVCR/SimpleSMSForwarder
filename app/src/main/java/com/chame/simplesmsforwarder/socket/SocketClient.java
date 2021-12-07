package com.chame.simplesmsforwarder.socket;

import android.net.Uri;
import com.chame.simplesmsforwarder.sms.SmsMessage;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import java.util.Collections;


public class SocketClient {
    private Socket socket = null;
    private OnSocketLoginSuccessListener loginListener;
    private OnSocketFailureListener listener;
    private OnSocketMessageListener msgListener;

    public SocketClient(OnSocketFailureListener socketListener,
                        OnSocketLoginSuccessListener loginListener) {

        this.listener = socketListener;
        this.loginListener = loginListener;
    }

    public void connect(String ip, String port, String token, String phone, boolean https) {
        String scheme = https ? "https" : "http";
        String root = String.format("%s:%s", ip, port);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme).encodedAuthority(root);
        String uri = builder.build().toString();

        IO.Options options = IO.Options.builder()
                .setPath("/socket.io/")
                .setAuth(Collections.singletonMap("token", token))
                .setQuery(String.format("phone=%s", phone))
                .build();

        try {
            socket = IO.socket(uri, options);
        } catch (URISyntaxException e) {
            listener.onConnectionFailure(FailureCode.InvalidUri);
            return;
        }

        socket.on("error", args -> {
            listener.onConnectionFailure(FailureCode.ConnectionError);
            disconnect();
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            listener.onConnectionFailure(FailureCode.ConnectionError);
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            listener.onConnectionFailure(FailureCode.ConnectionError);
        });

        socket.on("message", message -> {
            String msg = message[0].toString();

            if (msg.equals("Authenticated")) {
                if (loginListener != null) loginListener.onConnectionSuccess();
            } else if (msg.equals("Unauthenticated")) {
                if (listener != null) listener.onConnectionFailure(FailureCode.AuthError);
                disconnect();
            } else {
                if (msgListener != null) msgListener.onMessage(msg);
            }
        });

        socket.connect();
    }

    public void notifyReady() {
        socket.emit("feedback", "Ready");
    }

    public void notifyReschedule(SmsMessage sms) {
        socket.emit("reschedule", sms.getOriginalMessage());
    }

    public void disconnect() {
        if (socket != null) socket.disconnect();
        socket = null;
    }

    public void setMsgListener(OnSocketMessageListener msgListener) {
        this.msgListener = msgListener;
    }

    public void setFailureListener(OnSocketFailureListener loginFailureListener) {
        this.listener = loginFailureListener;
    }

    public void setSuccessListener(OnSocketLoginSuccessListener loginSuccessListener) {
        this.loginListener = loginSuccessListener;
    }

    public boolean isConnected() {
        if (socket == null) return false;
        return socket.connected();
    }

    public enum FailureCode{
        InvalidUri,
        ConnectionError,
        AuthError
    }

    public interface OnSocketFailureListener {
        void onConnectionFailure(FailureCode f);
    }

    public interface OnSocketLoginSuccessListener {
        void onConnectionSuccess();
    }

    public interface OnSocketMessageListener {
        void onMessage(String msg);
    }
}
