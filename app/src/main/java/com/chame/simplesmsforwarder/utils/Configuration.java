package com.chame.simplesmsforwarder.utils;

import android.content.Context;

import java.io.*;
import java.util.*;

public class Configuration {
    private final Properties properties;
    private final Context context;

    // Initializes the Hashmap and loads the properties into it.
    public Configuration(Context context) {
        this.context = context;
        properties = load();
    }

    // Returns the content of a Properties element. As the properties files is
    // only loaded once and is checked for errors at that time, it shouldn't
    // be possible to consult a non-existing Properties element.
    public String getProperty(String property) {
        return properties.getProperty(property);
    }

    public void setProperty(String property, String value) {
        properties.setProperty(property, value);
    }

    public boolean hasCredentials(){
        return getProperty("ip") != null && getProperty("port") != null && getProperty("token") != null;
    }

    public void save(){
        String filename = "SimpleSMSForwarder.properties";
        File propertiesFile = new File(context.getFilesDir(), "config/" + filename);
        propertiesFile.getParentFile().mkdirs();
        try {
            FileOutputStream fileos = new FileOutputStream(propertiesFile);
            this.properties.store(fileos, filename);
            fileos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties load() {
        String filename = "SimpleSMSForwarder.properties";
        File propertiesFile = new File(context.getFilesDir(), "config/" + filename);
        Properties locProperties = new Properties();
        Properties defProperties = new Properties();

        try {
            propertiesFile.getParentFile().mkdirs();
            defProperties.load(context.getAssets().open(filename));

            if (propertiesFile.exists()) {
                FileInputStream fileis = new FileInputStream(propertiesFile);
                locProperties.load(fileis);
                fileis.close();
                return locProperties;
            }

            FileOutputStream fileos = new FileOutputStream(propertiesFile);
            defProperties.store(fileos, filename);
            fileos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return defProperties;
    }
}