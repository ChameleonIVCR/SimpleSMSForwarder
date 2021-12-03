package com.chame.simplesmsforwarder.models;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.sms.SmsController;
import com.chame.simplesmsforwarder.sms.SmsMessage;
import com.chame.simplesmsforwarder.socket.SocketClient;
import com.chame.simplesmsforwarder.utils.ThreadingAssistant;
import com.chame.simplesmsforwarder.utils.Utils;
import org.json.JSONException;


public class AppViewModel extends AndroidViewModel {
    private static final ThreadingAssistant thAssistant = new ThreadingAssistant();
    private final int heartBeatTime = 5;
    private EventViewModel eventPropagator;
    private final SmsController smsController;
    private SocketClient socketClient;
    private SmsMessage retrySms;

    private boolean isReady = false;
    private boolean isAtReadyTimeout = false;
    private long lastSentAt;


    public AppViewModel(@NonNull Application application) {
        super(application);
        smsController = new SmsController(application);
        smsController.setOnSmsSendFailureListener(this::onSmsFailure);
        smsController.setOnSmsSendSuccessListener(this::onSmsSuccess);
    }

    public void setEventPropagator(EventViewModel viewModel){
        eventPropagator = viewModel;
    }

    // region Socket client login

    public void setSocketClientAndConnect(SocketClient.OnSocketFailureListener sf,
                                SocketClient.OnSocketLoginSuccessListener ss,
                                String ip,
                                String port,
                                String token,
                                boolean https) {

        socketClient = new SocketClient(sf, ss);
        socketClient.connect(
                ip,
                port,
                token,
                https
        );
    }

    public void setSocketClientMainListeners() {
        socketClient.setFailureListener(this::onSocketFailure);
        socketClient.setMsgListener(this::onSocketMessage);
        socketClient.setSuccessListener(this::onSocketReconnect);
        thAssistant.socketHeartBeat(this::heartBeatUpdateInfo, heartBeatTime);
        checkRadioIsUp();
    }

    public void disconnectSocket() {
        if (socketClient != null) socketClient.disconnect();
    }

    // endregion

    // region Socket listeners
    public void onSocketMessage(String jsonMessage) {
        SmsMessage sms;

        try {
            sms = new SmsMessage(jsonMessage);
        } catch (JSONException e) {
            Utils.runOnUiThread(() -> MainActivity.getInstance()
                    .setSnackbar("Received SMS wasn't correctly encoded, continuing..."));
            socketClient.notifyReady();
            isReady = true;
            retrySms = null;
            return;
        }

        sendSms(sms);
    }

    private void onSocketReconnect() {
        eventPropagator.setSocketStatus(socketClient.isConnected());

        // If we are ready to send more messages, notify the server. If we are not, wait
        // for current running operations to notify so themselves.
        if (isReady) notifyReady();
    }

    public void onSocketFailure(SocketClient.FailureCode f) {
        eventPropagator.setSocketStatus(socketClient.isConnected());
    }
    // endregion

    // region Sms listeners
    public void onSmsSuccess(SmsMessage msg) {
        eventPropagator.setGsmStatus(true);
        eventPropagator.incrementSentMessages();
        if (msg.isTest() && retrySms != null) eventPropagator.incrementFailedMessages();

        notifyReady();
        isReady = true;
        retrySms = null;
    }

    public void onSmsFailure(SmsController.FailureCode f, SmsMessage smsMessage) {
        String msg = null;
        if (retrySms == null) retrySms = smsMessage;
        retrySms.incrementAttempts();

        if (f == SmsController.FailureCode.NoPermission) {
            Utils.runOnUiThread(() -> MainActivity.getInstance()
                    .setSnackbar("SMS permissions were denied, please restart the app and give SMS permissions."));
            return;
        }

        // If a test SMS didn't reach its destination, stop execution completely.
        if (smsMessage.isTest()) {
            // If we have any pending SMS to send, notify the server that it needs to be rescheduled.
            if (retrySms != null && !retrySms.isTest()) {
                socketClient.notifyReschedule(retrySms);
                eventPropagator.incrementRescheduledMessages();
            }
            retrySms = null;
            postponeExecution();
            return;
        }

        if (retrySms.getAttempts() == 3) {
            // Check if the radio connection is still up.
            checkRadioIsUp();
            return;
        }

        switch(f) {
            case NullPdu:
                msg = "Mobile carrier informs the PDU is null. Please check your signal strength.";
                break;
            case Canceled:
                msg = "A message was canceled before it was sent, if this wasn't you, close your default SMS app";
                break;
            case RadioOff:
                msg = "Can not send an SMS as the phone has disabled GSM, ej: Airplane mode";
                break;
            case NoService:
                msg = "Can not send an SMS due to low signal, or no service. Please check your signal strength";
                break;
            case GenericFailure:
                msg = "Generic failure issued, please check your default SMS sim card if you have dual sims";
                break;
        }

        pauseExecution();
        MainActivity.getInstance().setSnackbar(msg);
    }
    // endregion

    // region General purpose listeners

    private void checkRadioIsUp(){
        // TODO Configure this in settings
        Utils.runOnUiThread(() -> MainActivity.getInstance().setSnackbar("Sending verification SMS to check if network is up..."));
        sendSms(new SmsMessage(
                MainActivity.getInstance().getDataAssistant().getConfiguration().getProperty("test-phone"),
                "SSF:Testing network at " + System.currentTimeMillis()));
    }

    private void retrySmsSend() {
        sendSms(retrySms);
    }

    private void pauseExecution() {
        eventPropagator.setGsmStatus(false);
        thAssistant.postTimeout(this::retrySmsSend, 5);
    }

    private void postponeExecution() {
        eventPropagator.setCurrentEvent(EventViewModel.StatusEvent.Timeout);
        eventPropagator.setGsmStatus(false);
        // TODO tell the frontend that the app has stopped due to network issues, and will restart in 2 minutes.
        Utils.runOnUiThread(() -> MainActivity.getInstance()
                .setSnackbar("Couldn't send test SMS, assuming network is down, retrying in 2 minutes..."));

        thAssistant.postTimeout(this::checkRadioIsUp, 120);
    }

    private void heartBeatUpdateInfo() {
        eventPropagator.setSocketStatus(socketClient.isConnected());
        eventPropagator.incrementRunningTime(heartBeatTime);

        if (Utils.isAirplaneModeOn(MainActivity.getInstance())) {
            eventPropagator.setGsmStatus(false);
            Utils.runOnUiThread(() -> {
                MainActivity.getInstance().setSnackbar("Airplane mode detected, please disable it before resuming...");
            });
        }
    }

    // endregion

    private void notifyReady() {
        long timeDeltaSeconds = (System.currentTimeMillis() - lastSentAt) / 1000;
        long timeout = Long.parseLong(
                MainActivity.getInstance().getDataAssistant().getConfiguration().getProperty("timeout")
        );

        if (timeDeltaSeconds >= timeout) {
            eventPropagator.setCurrentEvent(EventViewModel.StatusEvent.Ready);
            socketClient.notifyReady();
        } else {
            if (!isAtReadyTimeout) {
                long futureTime = timeout - timeDeltaSeconds;
                isAtReadyTimeout = true;
                thAssistant.postReadyTimeout(() -> {
                            eventPropagator.setCurrentEvent(EventViewModel.StatusEvent.Ready);
                            isAtReadyTimeout = false;
                            socketClient.notifyReady();
                        }, futureTime
                );
            }
        }
    }

    private void sendSms(SmsMessage sms) {
        //smsController.sendSMS(sms);
        thAssistant.postSms(() -> smsController.sendSMS(sms));
        isReady = false;

        eventPropagator.setCurrentEvent(EventViewModel.StatusEvent.Sending);
        lastSentAt = System.currentTimeMillis();
    }
}
