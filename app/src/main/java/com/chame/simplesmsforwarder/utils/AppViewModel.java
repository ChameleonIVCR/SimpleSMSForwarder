package com.chame.simplesmsforwarder.utils;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.chame.simplesmsforwarder.MainActivity;
import com.chame.simplesmsforwarder.sms.SmsController;
import com.chame.simplesmsforwarder.sms.SmsMessage;
import com.chame.simplesmsforwarder.socket.SocketClient;
import org.json.JSONException;


public class AppViewModel extends AndroidViewModel {
    private static ThreadingAssistant thAssistant = new ThreadingAssistant();
    private SocketClient socketClient;
    private SmsController smsController;
    private SmsMessage retrySms;


    public AppViewModel(@NonNull Application application) {
        super(application);
        smsController = new SmsController(application);
        smsController.setOnSmsSendFailureListener(this::onSmsFailure);
        smsController.setOnSmsSendSuccessListener(this::onSmsSuccess);
    }

    public void setSocketClient(SocketClient socket) {
        socketClient = socket;
        socketClient.setFailureListener(this::onSocketFailure);
        socketClient.setMsgListener(this::onSocketMessage);
        checkRadioIsUp();
    }

    // region Socket Listeners
    public void onSocketMessage(String jsonMessage) {
        SmsMessage sms;
        try {
            sms = new SmsMessage(jsonMessage);
        } catch (JSONException e) {
            MainActivity.getInstance().setSnackbar("Received SMS wasn't correctly encoded, continuing...");
            return;
        }

        thAssistant.postSms(() -> smsController.sendSMS(sms));
    }

    public void onSocketFailure(SocketClient.FailureCode f) {

    }
    // endregion


    // region Sms Listeners
    public void onSmsSuccess(SmsMessage msg) {
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
            // Check if the network connection is still up.
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

    private void checkRadioIsUp(){
        // TODO Configure this in settings
        MainActivity.getInstance().setSnackbar("Sending verification SMS to check if network is up...");
        thAssistant.postSms(() -> smsController.sendSMS(new SmsMessage("+573209811646", "SimpleSmsForwarder: Testing network")));
    }

    private void retrySmsSend() {
        thAssistant.postSms(() -> smsController.sendSMS(retrySms));
    }

    private void pauseExecution() {
        thAssistant.postTimeout(this::retrySmsSend, 5);
    }

    private void postponeExecution() {
        // TODO tell the frontend that the app has stopped due to network issues, and will restart in 5 minutes.
        MainActivity.getInstance().setSnackbar("Execution has been paused for 2 minutes due to network errors.");
        thAssistant.postTimeout(this::retrySmsSend, 120);
    }
}
