package com.fedcba.attendance;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "fedcba_attendance_alarms";
    private static final String CHANNEL_NAME = "Attendance Alarms";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Reschedule alarms on boot
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            AlarmScheduler.scheduleAlarms(context);
            return;
        }

        int alarmId = intent.getIntExtra("alarm_id", 0);
        String label = intent.getStringExtra("alarm_label");
        String message = intent.getStringExtra("alarm_message");

        // Vibrate
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(new long[]{0, 300, 200, 300, 200, 300}, -1);
        }

        // Show notification
        showNotification(context, alarmId, label, message);

        // Reschedule for next day
        AlarmScheduler.scheduleAlarms(context);
    }

    private void showNotification(Context context, int id, String label, String message) {
        NotificationManager notifManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("FEDCBA Attendance V2 reminders");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});
            notifManager.createNotificationChannel(channel);
        }

        // Open app when notification tapped
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, openIntent, flags);

        // Alarm sound
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null) {
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("FEDCBA Attendance V2 - " + label)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(new long[]{0, 300, 200, 300, 200, 300})
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true); // Show heads-up notification

        notifManager.notify(id, builder.build());
    }
}
