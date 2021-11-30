package com.chame.simplesmsforwarder.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

public class Utils {
    public static void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressWarnings({"deprecation"})
    public static boolean isAirplaneModeOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
}
