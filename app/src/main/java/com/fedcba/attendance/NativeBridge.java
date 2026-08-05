package com.fedcba.attendance;

import android.content.Context;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class NativeBridge {

    private final Context context;

    public NativeBridge(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void vibrate(int duration) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public boolean isOnline() {
        android.net.ConnectivityManager cm =
            (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @JavascriptInterface
    public String getDeviceInfo() {
        return android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
    }
}
