package com.example.gallery.utils;

import static androidx.core.app.ActivityCompat.startIntentSenderForResult;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
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

    public static List<String> getBucketIds(Context context) {
        List<String> bucketIds = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        // Truy vấn dữ liệu từ bảng MediaStore.Images.Media để lấy danh sách bucket ID.
        Cursor cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media.BUCKET_ID},
                null,
                null,
                null);

        if (cursor != null) {
            try {
                // Duyệt qua tất cả các dòng trong cursor và thêm bucket ID vào danh sách.
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String bucketId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    bucketIds.add(bucketId);
                }
            } finally {
                cursor.close(); // Đảm bảo đóng cursor sau khi sử dụng xong.
            }
        }

        // Loại bỏ các ID trùng lặp bằng cách sử dụng một HashSet.
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
                        return o1.bucketName.compareTo(o2.bucketName) * sortDirection;
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
        Log.d("MediaListener", "Current listeners: " + listeners.size());
    }

    public void unregisterListener(MediaContentObserver.OnMediaUpdateListener listener) {
        listeners.remove(listener);
        Log.d("MediaListener", "Current listeners: " + listeners.size());
    }

    public void fetchMedia(boolean forceFetch) {
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

    public static ArrayList<Uri> getContentUrisForFiles(Context context, List<MediaModel> mediaDelete) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projectionImage = {MediaStore.Images.Media._ID};
        String selectionImage = MediaStore.Images.Media.DATA + "=?";

        String[] projectionVideo = {MediaStore.Video.Media._ID};
        String selectionVideo = MediaStore.Video.Media.DATA + "=?";

        List<String> filePaths = mediaDelete.stream().map(mediaModel -> mediaModel.data).collect(Collectors.toList());

        ArrayList<Uri> contentUris = new ArrayList<>();
        for (String filePath : filePaths) {
            String[] selectionArgs = new String[]{filePath};
            Cursor cursorImage = contentResolver.query(imageUri, projectionImage, selectionImage, selectionArgs, null);
            Cursor cursorVideo = contentResolver.query(videoUri, projectionVideo, selectionVideo, selectionArgs, null);
            try {
                if (cursorImage != null && cursorImage.moveToFirst()) {
                    int columnIndex = cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    long fileId = cursorImage.getLong(columnIndex);
                    contentUris.add(Uri.withAppendedPath(imageUri, String.valueOf(fileId)));
                }else if (cursorVideo != null && cursorVideo.moveToFirst()) {
                    int columnIndex = cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    long fileId = cursorVideo.getLong(columnIndex);
                    contentUris.add(Uri.withAppendedPath(videoUri, String.valueOf(fileId)));
                }
            } finally {
                if (cursorImage != null) {
                    cursorImage.close();
                }
                if (cursorVideo != null) {
                    cursorVideo.close();
                }
            }
        }
        return contentUris;
    }

    public static void deleteMediaFiles(ContentResolver contentResolver, ArrayList<MediaModel> mediaDelete) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            ArrayList<Uri> uris = MediaFetch.getContentUrisForFiles(instance.context, mediaDelete);

            PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris);
            try {
                startIntentSenderForResult(instance.mainActivity, pendingIntent.getIntentSender(), 0, null, 0, 0, 0, null);
            } catch (IntentSender.SendIntentException e) {
                throw new RuntimeException(e);
            }
        }else {
            if(!mediaDelete.isEmpty()) {
                Iterator<MediaModel> iterator = mediaDelete.iterator();
                while (iterator.hasNext()) {
                    MediaFetch.MediaModel mediaModel = iterator.next();
                    deleteMediaFile(contentResolver, mediaModel.data);
                    iterator.remove();
                }
            }
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
                    String data = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String dateTaken = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    String dateAdded = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String duration = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));

                    mediaList.add(new MediaModel(bucketName, bucketID, data, dateTaken, dateAdded, duration));
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
                    String bucketName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String data = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String dateTaken = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    String dateAdded = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String duration = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));

                    mediaList.add(new MediaModel(bucketName, bucketID, data, dateTaken, dateAdded, duration));
                }
            } finally {
                videoCursor.close();
            }
        }

        return mediaList;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("Media", "MediaFetch is destroyed");
    }

    public static class MediaModel implements Parcelable {
        public static final Creator<MediaModel> CREATOR = new Creator<MediaModel>() {
            @Override
            public MediaModel createFromParcel(Parcel in) {
                return new MediaModel(in);
            }

            @Override
            public MediaModel[] newArray(int size) {
                return new MediaModel[size];
            }
        };
        public String bucketName;  // Album name
        public String bucketID;    // Unique album identifier
        public String data;        // Filepath
        public String dateTaken;   // Date recorded or taken by device camera
        public String dateAdded;   // Date MediaStore added
        public String duration;    // Video duration

        public MediaModel(String bucketName, String bucketID, String data, String dateTaken, String dateAdded, String duration) {
            this.bucketName = bucketName;
            this.bucketID = bucketID;
            this.data = data;
            this.dateTaken = dateTaken;
            this.dateAdded = dateAdded;
            this.duration = duration;
        }

        // Parcelable implementation
        protected MediaModel(Parcel in) {
            bucketName = in.readString();
            bucketID = in.readString();
            data = in.readString();
            dateTaken = in.readString();
            dateAdded = in.readString();
            duration = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(bucketName);
            dest.writeString(bucketID);
            dest.writeString(data);
            dest.writeString(dateTaken);
            dest.writeString(dateAdded);
            dest.writeString(duration);
        }

    }
}
