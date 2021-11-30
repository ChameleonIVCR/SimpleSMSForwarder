package com.chame.simplesmsforwarder.sms;

import org.json.JSONException;
import org.json.JSONObject;

public class SmsMessage {
    final private String phone;
    final private String message;
    private final boolean isTest;
    private String originalMessage;
    private int attempts = 0;

    public SmsMessage(String message) throws JSONException {
        originalMessage = message;
        JSONObject json = new JSONObject(message);
        if (!json.has("cell_phone") || json.has("message")) throw new JSONException("Key error");

        String incoming_message = json.getString("message");
        final int messageLength = Math.min(incoming_message.length(), 160);
        this.message = incoming_message.substring(0, messageLength);

        String phoneNumber = json.getString("cell_phone");
        this.phone = phoneNumber.startsWith("+57") ? phoneNumber : String.format("+57%s", phoneNumber);
        this.isTest = false;
    }

    public SmsMessage(String phone, String message) {
        this.phone = phone;
        this.message = message;
        this.isTest = true;
    }


    public String getPhone() {
        return phone;
    }

    public String getMessage() {
        return message;
    }

    public int getAttempts() {
        return attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isTest() {
        return isTest;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }
}
