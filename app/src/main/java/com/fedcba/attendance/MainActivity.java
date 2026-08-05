package com.fedcba.attendance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.webkit.CookieManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String WEBSITE_URL = "https://www.attendance.v2.fedcba.site/";
    private static final int LOCATION_PERMISSION_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_CODE = 1002;

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        checkLocationPermission();   // 🔥 Request location permission

        setupWebView();
        webView.loadUrl(WEBSITE_URL);
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            java.util.List<String> toRequest = new java.util.ArrayList<>();

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            // Kailangan din ang notification permission (Android 13+) para
            // lumabas yung persistent notification ng dispatch tracking.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }

            if (!toRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        toRequest.toArray(new String[0]),
                        LOCATION_PERMISSION_CODE);
            } else {
                requestBackgroundLocationIfNeeded();
            }
        }
    }

    // Background location ay hiwalay na request sa Android 10+ (dapat
    // naka-grant muna ang foreground/fine location bago ito hingin).
    // Ito ang nagpapagana sa continuous GPS tracking kahit naka-minimize
    // ang app habang naka-Dispatch.
    private void requestBackgroundLocationIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            boolean fineGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (!fineGranted) {
                Toast.makeText(this,
                        "Location permission is required for attendance.",
                        Toast.LENGTH_LONG).show();
            } else {
                requestBackgroundLocationIfNeeded();
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            boolean bgGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            if (!bgGranted) {
                Toast.makeText(this,
                        "Para tuloy-tuloy ang Dispatch tracking kahit naka-minimize, i-allow ang \"Allow all the time\" sa Location permission (Settings > Apps > FEDCBA Attendance V2 > Permissions).",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setGeolocationEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setUserAgentString(
                settings.getUserAgentString() + " FEDCBAApp/1.0 Android"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("dcba") || url.contains("logattendance") || url.startsWith("about:")) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view,
                                        WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    view.loadUrl(WEBSITE_URL);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url,
                                     String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("FEDCBA Attendance V2")
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("FEDCBA Attendance V2")
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setNegativeButton("Cancel", (d, w) -> result.cancel())
                        .setCancelable(false)
                        .show();
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    callback.invoke(origin, true, false);

                } else {
                    callback.invoke(origin, false, false);
                }
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        webView.addJavascriptInterface(new Object() {

            @android.webkit.JavascriptInterface
            public boolean isOnline() {
                ConnectivityManager cm =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                return netInfo != null && netInfo.isConnected();
            }

            @android.webkit.JavascriptInterface
            public void showToast(String msg) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this,
                                msg,
                                Toast.LENGTH_SHORT).show()
                );
            }

            // Tinatawag ng website (dashboard.php) kapag naka-active na ang
            // isang Dispatch trip. Nagsisimula ng foreground service na
            // tuloy-tuloy magpapadala ng GPS location papunta sa
            // dispatch_ping.php kahit naka-minimize o naka-lock ang phone.
            @android.webkit.JavascriptInterface
            public void startDispatchTracking(String tripId) {
                String cookie = CookieManager.getInstance().getCookie(WEBSITE_URL);

                Intent intent = new Intent(MainActivity.this, DispatchTrackingService.class);
                intent.setAction("START");
                intent.putExtra("tripId", tripId);
                intent.putExtra("cookie", cookie);
                intent.putExtra("baseUrl", WEBSITE_URL);

                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                });
            }

            // Tinatawag pag Mark Arrival na (o na-end na ang trip) -- itigil
            // ang background tracking service.
            @android.webkit.JavascriptInterface
            public void stopDispatchTracking() {
                Intent intent = new Intent(MainActivity.this, DispatchTrackingService.class);
                intent.setAction("STOP");
                runOnUiThread(() -> startService(intent));
            }
        }, "AndroidApp");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("FEDCBA Attendance V2")
                    .setMessage("Gusto mo bang lumabas sa app?")
                    .setPositiveButton("Oo", (d, w) -> finish())
                    .setNegativeButton("Hindi", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
