package com.fedcba.attendance;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Foreground service para sa Dispatch/Arrival tracking. Basta naka-dispatch,
 * tumatakbo ito kahit naka-minimize o naka-lock ang phone -- hindi tulad ng
 * JS geolocation sa WebView na huminto pag naka-background. Direkta itong
 * nag-po-post ng pings papunta sa employee/dispatch_ping.php gamit ang
 * session cookie na kinuha mula sa WebView.
 */
public class DispatchTrackingService extends Service implements LocationListener {

    private static final String TAG = "DispatchTracking";
    private static final String CHANNEL_ID = "dispatch_tracking_channel";
    private static final int NOTIF_ID = 2001;

    // "1-2 minutes" -- balanced na setting, hindi masyadong mabilis maubos
    // ng baterya pero sapat pa rin para sa Dispatch/Arrival tracking.
    private static final long UPDATE_INTERVAL_MS = 90000L; // 1.5 minuto
    private static final float UPDATE_MIN_DISTANCE_M = 0f;

    private LocationManager locationManager;
    private ExecutorService networkExecutor;

    private String tripId;
    private String cookie;
    private String baseUrl;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        networkExecutor = Executors.newSingleThreadExecutor();
        createChannel();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        if ("STOP".equals(intent.getAction())) {
            stopTracking();
            return START_NOT_STICKY;
        }

        tripId = intent.getStringExtra("tripId");
        cookie = intent.getStringExtra("cookie");
        baseUrl = intent.getStringExtra("baseUrl");

        startForeground(NOTIF_ID, buildNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Dispatch Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tuloy-tuloy na pag-track ng lokasyon habang naka-dispatch");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FEDCBA Attendance V2")
                .setContentText("Naka-dispatch — tina-track ang lokasyon mo")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void startLocationUpdates() {
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, UPDATE_INTERVAL_MS, UPDATE_MIN_DISTANCE_M, this);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL_MS, UPDATE_MIN_DISTANCE_M, this);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Walang location permission, hindi ma-start ang tracking", e);
            stopTracking();
        }
    }

    private void stopTracking() {
        try {
            locationManager.removeUpdates(this);
        } catch (Exception ignored) {
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        sendPing(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void sendPing(double lat, double lng) {
        if (tripId == null || baseUrl == null) {
            return;
        }
        final String locationStr = String.format(Locale.US, "%.6f,%.6f", lat, lng);

        networkExecutor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(baseUrl + "employee/dispatch_ping.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                if (cookie != null) {
                    conn.setRequestProperty("Cookie", cookie);
                }

                String body = "action=ping&trip_id=" + URLEncoder.encode(tripId, "UTF-8")
                        + "&location=" + URLEncoder.encode(locationStr, "UTF-8");

                try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                    os.writeBytes(body);
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "Ping sent (" + locationStr + ") -> HTTP " + code);
            } catch (Exception e) {
                Log.e(TAG, "Ping failed", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            locationManager.removeUpdates(this);
        } catch (Exception ignored) {
        }
        networkExecutor.shutdown();
        super.onDestroy();
    }
}
