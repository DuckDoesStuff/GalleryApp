package com.example.gallery.utils.media;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.example.gallery.utils.GalleryDB;
import com.example.gallery.utils.MediaModel;

import java.util.ArrayList;

public class MediaStoreService extends Service {
    private ArrayList<MediaModel> mediaModelsToAdd;
    private ArrayList<MediaModel> mediaModelsToDelete;
    private ContentObserver contentObserver;

    @Override
    public void onCreate() {
        // Query the MediaStore for new media here
        // Update SQLite database with the retrieved information here
        // Sync with firestore if authenticated
        contentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                // Query the MediaStore for new media here
                queryMediaStore();
                // Update SQLite database with the retrieved information here
                addToMediaTable(mediaModelsToAdd);
                removeFromMediaTable(mediaModelsToDelete);
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


    private void queryMediaStore() {
        // Query the MediaStore for new media here
        ArrayList<MediaModel> mediaList = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.MIME_TYPE
        };

        Cursor imageCursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );
        GalleryDB db = new GalleryDB(this);
        if (imageCursor != null) {
            try {
                while (imageCursor.moveToNext()) {
                    String albumName = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String type = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String localPath = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    int mediaID = imageCursor.getInt(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    long dateTaken = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    if (dateTaken == 0) {
                        dateTaken = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    }
                    int duration = imageCursor.getInt(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));
                    db.onNewImageToUpload(localPath);
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setBucketID(bucketID)
                            .setAlbumName(albumName)
                            .setType(type)
                            .setLocalPath(localPath)
                            .setMediaID(mediaID)
                            .setDateTaken(dateTaken)
                            .setDuration(duration);
                    mediaList.add(mediaModel);
                }
            } finally {
                imageCursor.close();
            }
        }

        Cursor videoCursor = this.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.MediaColumns.DATE_TAKEN + " DESC"
        );
        if (videoCursor != null) {
            try {
                while (videoCursor.moveToNext()) {
                    String albumName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));
                    String type = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String localPath = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    int mediaID = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    long dateTaken = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                    if(dateTaken == 0) {
                        dateTaken = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    }
                    int duration = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    db.onNewImageToUpload(localPath);
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setBucketID(bucketID)
                            .setAlbumName(albumName)
                            .setType(type)
                            .setLocalPath(localPath)
                            .setMediaID(mediaID)
                            .setDateTaken(dateTaken)
                            .setDuration(duration);
                    mediaList.add(mediaModel);
                }
            } finally {
                videoCursor.close();
            }
        }


        ArrayList<MediaModel> dbAllMedia = db.getAllMedia();

        // Check for new media
        mediaModelsToAdd = new ArrayList<>();
        mediaModelsToAdd = mediaList.stream().filter(mediaModel -> !dbAllMedia.contains(mediaModel)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // Check for deleted media
        mediaModelsToDelete = new ArrayList<>();
        mediaModelsToDelete = dbAllMedia.stream().filter(mediaModel -> !mediaList.contains(mediaModel)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void addToMediaTable(ArrayList<MediaModel> mediaModels) {
        // Update SQLite database with the retrieved information here
        try(GalleryDB galleryDB = new GalleryDB(this)) {
            // Update the database
            galleryDB.addToMediaTable(mediaModels);
        }
        catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void removeFromMediaTable(ArrayList<MediaModel> mediaModels) {
        // Update SQLite database with the retrieved information here
        try(GalleryDB galleryDB = new GalleryDB(this)) {
            // Update the database
            galleryDB.removeFromMediaTable(mediaModels);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
