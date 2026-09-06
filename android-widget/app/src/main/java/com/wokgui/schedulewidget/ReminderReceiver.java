package com.wokgui.schedulewidget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "schedule_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String label = intent.getStringExtra(ReminderScheduler.EXTRA_LABEL);
        String room = intent.getStringExtra(ReminderScheduler.EXTRA_ROOM);
        String start = intent.getStringExtra(ReminderScheduler.EXTRA_START);
        int minutes = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTES, 10);
        if (label == null || label.trim().isEmpty()) label = UiSettingsStore.t(context, "next");

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "Rappels de cours", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Rappels avant le début des cours");
            nm.createNotificationChannel(channel);
        }

        Intent open = new Intent(context, MainActivity.class).putExtra("open_mode", "today");
        PendingIntent pending = PendingIntent.getActivity(context, 7102, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        StringBuilder text = new StringBuilder();
        if (minutes > 0) text.append("Dans ").append(minutes).append(" min");
        else text.append("Maintenant");
        if (start != null && !start.isEmpty()) text.append(" · ").append(start);
        if (room != null && !room.trim().isEmpty()) text.append(" · ").append(UiSettingsStore.t(context, "room")).append(" ").append(room.trim());

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL)
                : new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(label)
                .setContentText(text.toString())
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setShowWhen(true);
        nm.notify(7103, builder.build());
        ReminderScheduler.reschedule(context);
    }
}
