package com.chame.simplesmsforwarder.utils;

import android.content.Context;

public class DataAssistant {
    private final Configuration configuration;

    public DataAssistant(Context context) {
        configuration = new Configuration(context);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void close(){
        configuration.save();
    }
}
