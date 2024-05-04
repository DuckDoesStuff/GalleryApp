package com.example.gallery.utils;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.gallery.R;
import com.example.gallery.utils.database.MediaModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class UploadService extends Service {
    public static final String CHANNEL_ID = "UploadServiceChannel";
    public static final int SERVICE_NOTIFICATION_ID = 1;
    private NotificationCompat.Builder builder;
    private int count = 0;
    private int uploading = 0;
    private int failed = 0;
    private final Object lock = new Object();
    private static final int MAX_CONCURRENT_UPLOADS = 5;
    private Semaphore uploadSemaphore = new Semaphore(MAX_CONCURRENT_UPLOADS);
    private FirebaseUser user;
    private StorageReference userRoot;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("UploadService", "Service created");
        createNotificationChannel();
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setProgress(100, 0, true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UploadService", "Service started with intent");
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        userRoot = FirebaseStorage.getInstance().getReference().child(user.getUid());

        ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("selectedMedia");
        if (mediaModels == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        synchronized (lock) {
            count += mediaModels.size();
        }

        builder.setContentTitle("Uploading " + count + " images");
        // Start the foreground service with notification
        startForeground(SERVICE_NOTIFICATION_ID, builder.build());
        startConcurrentUpload(mediaModels);

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

    @SuppressLint("MissingPermission")
    private void finishUpload() {
        builder.setContentTitle("Upload complete")
                .setContentText("Uploaded total of " + count + " media" +
                        (failed > 0 ? " with " + failed + " failed uploads" : ""))
                .setProgress(0, 0, false);
        NotificationManagerCompat.from(this).notify(SERVICE_NOTIFICATION_ID, builder.build());
        stopForeground(false);
        count = 0;
        uploading = 0;
    }

    @SuppressLint("MissingPermission")
    private void startConcurrentUpload(ArrayList<MediaModel > mediaModels) {
        for (MediaModel mediaModel : mediaModels) {
            startUploadThread(mediaModel);
        }
    }

    @SuppressLint("MissingPermission")
    private void startUploadThread(MediaModel mediaModel) {
        new Thread(() -> {
            try {
                uploadSemaphore.acquire();
                String[] parts = mediaModel.localPath.split("/");
                String storagePath = parts[parts.length - 2] + "/" + parts[parts.length - 1];
                StorageReference mediaRef = userRoot.child(storagePath);


                StorageMetadata storageMetadata = new StorageMetadata.Builder()
                        .setContentType(mediaModel.type)
                        .build();

                Uri file = Uri.fromFile(new File(mediaModel.localPath));
                UploadTask uploadTask = mediaRef.putFile(file, storageMetadata);
                Log.d("UploadService", "Uploading media to cloud");
                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    synchronized (lock) {
                        uploading++;
                        if(uploading == count) {
                            finishUpload();
                            return;
                        }
                        builder.setProgress(count, uploading, false);
                        NotificationManagerCompat.from(this).notify(SERVICE_NOTIFICATION_ID, builder.build());
                        Log.d("UploadService", "Media uploaded to cloud");
                        try {
                            mediaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                Log.d("UploadService", "URI: " + uri.toString());
//                                mediaModel.cloudPath = uri.toString();
//                                try (GalleryDB db = new GalleryDB(this)) {
//                                    db.updateMedia(mediaModel);
//                                } catch (Exception e) {
//                                    Log.d("UploadService", "Failed to update media in database");
//                                    e.printStackTrace();
//                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(e -> {
                    // Handle failed uploads
                    uploading++;
                    failed++;
                    if(uploading == count) {
                        finishUpload();
                    }
                    Log.d("UploadService", "Failed to upload media to cloud");
                    Log.d("UploadService", "Media path: " + mediaModel.localPath);
                }).addOnProgressListener(snapshot -> {
                    // Handle current media progress updates
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                uploadSemaphore.release();
            }
        }).start();
    }

}
