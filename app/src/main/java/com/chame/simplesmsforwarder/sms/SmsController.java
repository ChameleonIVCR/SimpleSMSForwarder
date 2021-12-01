package com.chame.simplesmsforwarder.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;


public class SmsController {
    private final SmsManager smsManager;
    private final PendingIntent sentIntent;
    private final PendingIntent deliveredIntent;
    private onSmsSendFailureListener listener;
    private OnSmsSendSuccessListener successListener;
    private SmsMessage smsMessage;

    public SmsController(Context context) {
        smsManager = SmsManager.getDefault();

        sentIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent("SMS_SENT"),
                0
        );

        deliveredIntent = PendingIntent.getBroadcast(
                context,
                0,
                new  Intent("SMS_DELIVERED"),
                0
        );

        // region Register Callbacks
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setSmsSentState(getResultCode());
            }
        }, new IntentFilter("SMS_SENT"));

        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                setSmsDeliveredState(getResultCode());
            }
        }, new IntentFilter("SMS_DELIVERED"));
        // endregion
    }

    private void setSmsSentState(int resultCode) {
        FailureCode failureCode = null;

        switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                failureCode = FailureCode.GenericFailure;
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                failureCode = FailureCode.NoService;
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                failureCode = FailureCode.NullPdu;
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                failureCode = FailureCode.RadioOff;
                break;
        }

        if (failureCode != null && listener != null) {
            handleFailure(failureCode);
        }
    }

    private void setSmsDeliveredState(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (successListener != null) successListener.onSuccess(smsMessage);
                break;
            case Activity.RESULT_CANCELED:
                if (listener != null) handleFailure(FailureCode.Canceled);
                break;
        }
    }

    public void handleFailure(FailureCode f) {
        if (listener != null) listener.onFailure(f, smsMessage);
    }

    public void sendSMS(SmsMessage smsMessage) {
        this.smsMessage = smsMessage;
        try {
            smsManager.sendTextMessage(
                    smsMessage.getPhone(),
                    null,
                    smsMessage.getMessage(),
                    sentIntent,
                    deliveredIntent
            );
        } catch(SecurityException e) {
            if (listener != null) listener.onFailure(FailureCode.NoPermission, null);
        }
    }

    public void setOnSmsSendFailureListener(onSmsSendFailureListener listener){
        this.listener = listener;
    }

    public void setOnSmsSendSuccessListener(OnSmsSendSuccessListener successListener) {
        this.successListener = successListener;
    }

    public enum FailureCode{
        GenericFailure,
        NoService,
        NullPdu,
        RadioOff,
        Canceled,
        NoPermission
    }

    public interface onSmsSendFailureListener {
        void onFailure(FailureCode f, SmsMessage smsMessage);
    }

    public interface OnSmsSendSuccessListener {
        void onSuccess(SmsMessage smsMessage);
    }
}
