package com.example.gallery.utils.media;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.example.gallery.utils.database.DatabaseQuery;
import com.example.gallery.utils.database.GalleryDB;

public class MediaStoreService extends Service {
    private ContentObserver contentObserver;
    private DatabaseQuery databaseQuery;

    @Override
    public void onCreate() {
        // Query the MediaStore for new media here
        // Update SQLite database with the retrieved information here
        // Sync with firestore if authenticated
        databaseQuery = new DatabaseQuery(this);
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                // Query the MediaStore for new media here
                databaseQuery.queryMedia();
                databaseQuery.queryAlbum();
                // Update SQLite database with the retrieved information here
                databaseQuery.addToMediaTable(databaseQuery.mediaModelsToAdd);
                databaseQuery.addToAlbumTable(databaseQuery.albumModelsToAdd);
                databaseQuery.removeFromMediaTable(databaseQuery.mediaModelsToDelete);
                databaseQuery.removeFromAlbumTable(databaseQuery.albumModelsToDelete);

                GalleryDB.notifyMediaObservers();
                GalleryDB.notifyAlbumObservers();
                // Sync with firestore if authenticated
            }
        };

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver);
        Log.d("MediaStoreService", "Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Service logic
        return START_REDELIVER_INTENT; // Automatically restarts with the previous intent
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return IBinder instance if your service supports binding
        return null;
    }

    @Override
    public void onDestroy() {
        // Clean-up code
        getContentResolver().unregisterContentObserver(contentObserver);
        Log.d("MediaStoreService", "Service stopped");
    }
}
