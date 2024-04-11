package com.example.gallery.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.gallery.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class MediaFetch {
    public static final int SORT_ASC = 1;
    public static final int SORT_DESC = -1;
    public static final int SORT_BY_BUCKET_NAME = 1;
    public static final int SORT_BY_DATE_TAKEN = 2;
    public static final int SORT_BY_DURATION = 3;
    private static MediaFetch instance;
    private final Context context;
    private final ArrayList<MediaContentObserver.OnMediaUpdateListener> listeners;
    private ArrayList<MediaModel> mediaModelArrayList;
    public MainActivity mainActivity;

    public interface onDeleteCallback {
        void onDeleteResult();
    }

    public static List<String> getBucketIds(Context context) {
        List<String> bucketIds = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[]{MediaStore.Files.FileColumns.BUCKET_ID};

        Uri uri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + " IN (" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + "," +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")";

        Cursor cursor = contentResolver.query(
                uri,
                projection,
                selection,
                null,
                null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.BUCKET_ID));
                    bucketIds.add(bucketId);
                }
            } finally {
                cursor.close();
            }
        }

        Set<String> uniqueBucketIds = new HashSet<>(bucketIds);
        return new ArrayList<>(uniqueBucketIds);
    }
    private MediaFetch(@NonNull Context context) {
        this.context = context;
        listeners = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        MediaContentObserver mediaContentObserver = new MediaContentObserver(new Handler());
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
        contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
    }
    private MediaFetch(@NonNull Context context, MainActivity mainActivity) {
        this.context = context;
        this.mainActivity = mainActivity;
        listeners = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        MediaContentObserver mediaContentObserver = new MediaContentObserver(new Handler());
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
        contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
    }

    public static synchronized MediaFetch getInstance(Context context) {
        if (instance == null && context != null) {
            Log.d("Media", "MediaFetch is initialized");
            instance = new MediaFetch(context);
        }
        return instance;
    }

    public static synchronized MediaFetch getInstance(Context context, MainActivity mainActivity) {
        if (instance == null && context != null) {
            Log.d("Media", "MediaFetch is initialized");
            instance = new MediaFetch(context, mainActivity);
        }
        return instance;
    }

    public static void sortArrayListModel(@NonNull ArrayList<MediaModel> modelArrayList, int sortBy, int sortDirection) {
        if (modelArrayList.isEmpty()) return;
        modelArrayList.sort((o1, o2) -> {
            try {
                switch (sortBy) {
                    case SORT_BY_BUCKET_NAME:
                        return o1.albumName.compareTo(o2.albumName) * sortDirection;
                    case SORT_BY_DURATION:
                        return o1.duration.compareTo(o2.duration) * sortDirection;
                    case SORT_BY_DATE_TAKEN:
                        return o1.dateTaken.compareTo(o2.dateTaken) * sortDirection;
                    default:
                        return 0;
                }
            }
            catch (NullPointerException e) {
                return 0;
            }
        });
    }

    public static ArrayList<MediaModel> mediaFromBucketID(ArrayList<MediaModel> modelArrayList, String bucketID) {
        if (modelArrayList.isEmpty()) return modelArrayList;
        return (ArrayList<MediaModel>)
                modelArrayList.stream()
                        .filter(mediaModel -> Objects.equals(mediaModel.bucketID, bucketID))
                        .collect(Collectors.toList());
    }

    public void registerListener(MediaContentObserver.OnMediaUpdateListener listener) {
        listeners.add(listener);
        Log.d("Debug", "Current listeners: " + listeners.size());
    }

    public void unregisterListener(MediaContentObserver.OnMediaUpdateListener listener) {
        listeners.remove(listener);
        Log.d("MediaListener", "Current listeners: " + listeners.size());
    }

    public void fetchMedia(boolean forceFetch) {
        // Get the caller's class name and method name
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        String callerClassName = stackTraceElements[3].getClassName();
        String callerMethodName = stackTraceElements[3].getMethodName();

        // Print the caller's information
        new Thread(() -> {
            try {
                if (forceFetch || mediaModelArrayList == null || mediaModelArrayList.isEmpty()) {
                    mediaModelArrayList = getMedia();
                }
                Log.d("Media", "Fetched new media " + mediaModelArrayList.size() + " found" + " Forcefully: " + forceFetch);

                notifyMediaUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void deleteMediaFiles(ContentResolver contentResolver, ArrayList<MediaModel> mediaDelete, onDeleteCallback callback) {
        if(!mediaDelete.isEmpty()) {
            Iterator<MediaModel> iterator = mediaDelete.iterator();
            while (iterator.hasNext()) {
                MediaModel mediaModel = iterator.next();
                deleteMediaFile(contentResolver, mediaModel.path);
                iterator.remove();
            }
            callback.onDeleteResult();
        }
    }

    private static void deleteMediaFile(ContentResolver contentResolver, String filePath) {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = new String[]{ filePath };

        // Delete the file
        if(contentResolver.delete(mediaUri, selection, selectionArgs) == 0){
            mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            contentResolver.delete(mediaUri, selection, selectionArgs);
        }
    }

    private void notifyMediaUpdate() {
        for (MediaContentObserver.OnMediaUpdateListener listener : listeners) {
            listener.onMediaUpdate(mediaModelArrayList);
        }
    }

    private ArrayList<MediaModel> getMedia() {
        ArrayList<MediaModel> mediaList = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DURATION
        };

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.MediaColumns.BUCKET_ID + " ASC"
        );
        if (imageCursor != null) {
            try {
                while (imageCursor.moveToNext()) {
                    String bucketName = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String path = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String dateTaken = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    String dateAdded = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String duration = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));
                    String type = "image";
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    MediaModel mediaModel = new MediaModel(bucketName, bucketID, path, name, dateTaken, dateAdded, duration, type);
                    mediaList.add(mediaModel);
                }
            } finally {
                imageCursor.close();
            }
        }

        Cursor videoCursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.MediaColumns.BUCKET_ID + " ASC"
        );
        if (videoCursor != null) {
            try {
                while (videoCursor.moveToNext()) {
                    String bucketName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));
                    String path = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String dateTaken = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                    String dateAdded = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    String duration = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    String type = "video";
                    String name = path.substring(path.lastIndexOf('/') + 1);
                    MediaModel mediaModel = new MediaModel(bucketName, bucketID, path, name, dateTaken, dateAdded, duration, type);
                    mediaList.add(mediaModel);
                }
            } finally {
                videoCursor.close();
            }
        }

        return mediaList;
    }

    public static String getDirectoryPathFromBucketId(String bucketId) {
        String directoryPath = null;

        // Define the columns you want to retrieve
        String[] projection = { MediaStore.Files.FileColumns.DATA };

        // Define the selection criteria
        String selection = MediaStore.Files.FileColumns.BUCKET_ID + " = ?";

        // Arguments for the selection criteria
        String[] selectionArgs = { bucketId };

        // Sort the results

        // Execute the query using the content resolver
        Cursor cursor = instance.context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"), // URI for both images and videos
                projection,
                selection,
                selectionArgs,
                null
        );

        // Check if the cursor is not null and move to the first entry
        if (cursor != null && cursor.moveToFirst()) {
            // Get the index of the DATA column
            int dataIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

            // Get the directory path from the cursor
            directoryPath = cursor.getString(dataIndex);
        }

        // Close the cursor
        if (cursor != null) {
            cursor.close();
        }

        // Return the directory path
        return directoryPath.substring(0, directoryPath.lastIndexOf('/'));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("Media", "MediaFetch is destroyed");
    }

}
