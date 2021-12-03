package com.chame.simplesmsforwarder.models;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.chame.simplesmsforwarder.utils.Utils;


public class EventViewModel extends ViewModel {
    private final MutableLiveData<Boolean> socketOnline = new MutableLiveData<>();
    private final MutableLiveData<Boolean> gsmOnline = new MutableLiveData<>();
    private final MutableLiveData<Integer> sentMessages = new MutableLiveData<>();
    private final MutableLiveData<Integer> failedMessages = new MutableLiveData<>();
    private final MutableLiveData<Integer> rescheduledMessages = new MutableLiveData<>();
    private final MutableLiveData<Long> elapsedSeconds = new MutableLiveData<>();
    private final MutableLiveData<StatusEvent> currentEvent = new MutableLiveData<>();


    public EventViewModel() {
        socketOnline.setValue(false);
        gsmOnline.setValue(false);
        sentMessages.setValue(0);
        failedMessages.setValue(0);
        rescheduledMessages.setValue(0);
        elapsedSeconds.setValue(0L);
        currentEvent.setValue(StatusEvent.Ready);
    }

    public void setSocketStatus(boolean value) {
        Utils.runOnUiThread(() -> socketOnline.setValue(value));
    }

    public LiveData<Boolean> getSocketStatus(){
        return socketOnline;
    }

    public void setGsmStatus(boolean value) {
        Utils.runOnUiThread(() -> gsmOnline.setValue(value));
    }

    public LiveData<Boolean> getGsmStatus() {
        return gsmOnline;
    }

    public void incrementSentMessages() {
        Utils.runOnUiThread(() -> {
            if (sentMessages.getValue() == null) sentMessages.setValue(0);
            sentMessages.setValue(sentMessages.getValue() + 1);
        });
    }

    public LiveData<Integer> getSentMessages() {
        return sentMessages;
    }

    public void incrementFailedMessages() {
        Utils.runOnUiThread(() -> {
            if (failedMessages.getValue() == null) failedMessages.setValue(0);
            failedMessages.setValue(failedMessages.getValue() + 1);
        });
    }

    public LiveData<Integer> getFailedMessages() {
        return failedMessages;
    }

    public void incrementRescheduledMessages() {
        Utils.runOnUiThread(() -> {
            if (rescheduledMessages.getValue() == null) rescheduledMessages.setValue(0);
            rescheduledMessages.setValue(rescheduledMessages.getValue() + 1);
        });
    }

    public LiveData<Integer> getRescheduledMessages() {
        return rescheduledMessages;
    }

    public void incrementRunningTime(int seconds) {
        Utils.runOnUiThread(() -> {
            if (elapsedSeconds.getValue() == null) elapsedSeconds.setValue(0L);
            elapsedSeconds.setValue(elapsedSeconds.getValue() + seconds);
        });
    }

    public LiveData<Long> getRunningTime() {
        return elapsedSeconds;
    }

    public void setCurrentEvent(StatusEvent event) {
        Utils.runOnUiThread(() -> {
            if (currentEvent.getValue() == null) currentEvent.setValue(event);
            currentEvent.setValue(event);
        });
    }

    public LiveData<StatusEvent> getCurrentEvent() {
        return currentEvent;
    }

    public enum StatusEvent {
        Sending,
        Ready,
        Timeout
    }
}
