package com.chame.simplesmsforwarder.utils;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.sms.SmsController;
import com.chame.simplesmsforwarder.sms.SmsMessage;
import com.chame.simplesmsforwarder.socket.SocketClient;
import org.json.JSONException;


public class AppViewModel extends AndroidViewModel {
    private static final ThreadingAssistant thAssistant = new ThreadingAssistant();
    private final SmsController smsController;
    private SocketClient socketClient;
    private SmsMessage retrySms;

    // Mutable data
    private final MutableLiveData<Boolean> socketOnline;
    private final MutableLiveData<Boolean> gsmOnline;


    public AppViewModel(@NonNull Application application) {
        super(application);
        smsController = new SmsController(application);
        smsController.setOnSmsSendFailureListener(this::onSmsFailure);
        smsController.setOnSmsSendSuccessListener(this::onSmsSuccess);

        // Mutable data
        socketOnline = new MutableLiveData<>();
        socketOnline.setValue(false);
        gsmOnline = new MutableLiveData<>();
        gsmOnline.setValue(false);
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
        socketClient.setSuccessListener(() -> Utils.runOnUiThread(() -> socketOnline.setValue(socketClient.isConnected())));
        thAssistant.socketHeartBeat(this::heartBeatUpdateInfo, 5);
        checkRadioIsUp();
    }

    // endregion

    // region Mutable data
    public LiveData<Boolean> getSocketStatus() {
        return socketOnline;
    }

    public LiveData<Boolean> getGsmStatus() {
        return gsmOnline;
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
            return;
        }

        thAssistant.postSms(() -> smsController.sendSMS(sms));
    }

    public void onSocketFailure(SocketClient.FailureCode f) {
        Utils.runOnUiThread(() -> socketOnline.setValue(socketClient.isConnected()));
    }
    // endregion

    // region Sms listeners
    public void onSmsSuccess(SmsMessage msg) {
        Utils.runOnUiThread(() -> gsmOnline.setValue(true));

        socketClient.notifyReady();
        retrySms = null;
    }

    public void onSmsFailure(SmsController.FailureCode f, SmsMessage smsMessage) {
        String msg = null;
        if (retrySms == null) retrySms = smsMessage;
        retrySms.incrementAttempts();

        // If a test SMS didn't reach its destination, stop execution completely.
        if (smsMessage.isTest()) {
            // If we have any pending SMS to send, notify the server that it needs to be rescheduled.
            if (retrySms != null) socketClient.notifyReschedule(retrySms);
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
        thAssistant.postSms(() -> smsController.sendSMS(new SmsMessage(
                MainActivity.getInstance().getDataAssistant().getConfiguration().getProperty("test-phone"),
                "SimpleSmsForwarder: Testing network")
        ));
    }

    private void retrySmsSend() {
        thAssistant.postSms(() -> smsController.sendSMS(retrySms));
    }

    private void pauseExecution() {
        Utils.runOnUiThread(() -> gsmOnline.setValue(false));
        thAssistant.postTimeout(this::retrySmsSend, 5);
    }

    private void postponeExecution() {
        Utils.runOnUiThread(() -> gsmOnline.setValue(false));
        // TODO tell the frontend that the app has stopped due to network issues, and will restart in 2 minutes.
        Utils.runOnUiThread(() -> MainActivity.getInstance()
                .setSnackbar("Couldn't send test SMS, assuming network is down, retrying in 2 minutes..."));

        thAssistant.postTimeout(this::checkRadioIsUp, 120);
    }

    private void heartBeatUpdateInfo() {
        Utils.runOnUiThread(() -> socketOnline.setValue(socketClient.isConnected()));

        if (Utils.isAirplaneModeOn(MainActivity.getInstance())) {
            Utils.runOnUiThread(() -> {
                gsmOnline.setValue(false);
                MainActivity.getInstance().setSnackbar("Airplane mode detected, please disable it before resuming...");
            });
        }
    }

    // endregion
}
