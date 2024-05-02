package com.example.gallery.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gallery.R;

public class UploadService extends Service {
    public static final String CHANNEL_ID = "UploadServiceChannel";
    public static final int SERVICE_NOTIFICATION_ID = 1;
    private Notification notification;
    private NotificationCompat.Builder builder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        String notificationTitle = "Uploading Images";
        String notificationContent = "Uploading...";
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notificationTitle)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(100, 0, true)
                .setOngoing(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the foreground service with notification
        startForeground(SERVICE_NOTIFICATION_ID, builder.build());

        new Thread(() ->{
            // Do the work here
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                builder.setProgress(100, i, false);
                NotificationManagerCompat.from(this).notify(SERVICE_NOTIFICATION_ID, builder.build());
            }

            stopForeground(true);
        }).start();


        return START_STICKY;
    }

    private void createNotificationChannel() {
        CharSequence name = "Gallery cloud sync";
        String description = "Show information about cloud sync";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        channel.setSound(null, null);
        channel.enableVibration(false);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.createNotificationChannel(channel);
    }
}
