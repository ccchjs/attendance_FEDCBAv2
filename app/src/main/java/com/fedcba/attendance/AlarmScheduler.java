package com.fedcba.attendance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

public class AlarmScheduler {

    private static final int[][] ALARM_TIMES = {
        {8,  0,  0},
        {12, 0,  1},
        {13, 0,  2},
        {15, 0,  3},
        {17, 25, 4},
    };

    private static final String[] ALARM_LABELS = {
        "⏰ Time In", "☕ Break Out", "✅ Break In",
        "🍽️ Dinner Out", "🏁 Time Out"
    };

    private static final String[] ALARM_MESSAGES = {
        "Oras na para mag-TIME IN!",
        "Oras na para sa BREAK OUT!",
        "Balik na! BREAK IN na!",
        "Oras na para sa DINNER OUT!",
        "Uwian na! TIME OUT!"
    };

    public static void scheduleAlarms(Context context) {
        AlarmManager alarmManager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < ALARM_TIMES.length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, ALARM_TIMES[i][0]);
            calendar.set(Calendar.MINUTE, ALARM_TIMES[i][1]);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("alarm_id", ALARM_TIMES[i][2]);
            intent.putExtra("alarm_label", ALARM_LABELS[i]);
            intent.putExtra("alarm_message", ALARM_MESSAGES[i]);

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, ALARM_TIMES[i][2], intent, flags);

            // Gamitin ang setWindow para hindi kailangan ng exact alarm permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
                );
            }
        }
    }
}
