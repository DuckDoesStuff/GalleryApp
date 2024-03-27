package com.example.gallery.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediaFetch {
    public static final int SORT_ASC = -1;
    public static final int SORT_DESC = 1;
    public static final int SORT_BY_BUCKET_NAME = 1;
    public static final int SORT_BY_DATE_TAKEN = 2;
    public static final int SORT_BY_DATE_DURATION = 3;
    private static MediaFetch instance;
    private final Context context;
    private final ArrayList<MediaContentObserver.OnMediaUpdateListener> listeners;
    private ArrayList<MediaModel> mediaModelArrayList;
    private MediaFetch(@NonNull Context context) {
        this.context = context;
        listeners = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        MediaContentObserver mediaContentObserver = new MediaContentObserver(new Handler());
        contentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
        contentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, mediaContentObserver);
    }

    public static synchronized MediaFetch getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new MediaFetch(context);
        }
        return instance;
    }

    public static ArrayList<MediaModel> sortArrayListModel(@NonNull ArrayList<MediaModel> modelArrayList, int sortBy, int sortDirection) {
        if (modelArrayList.isEmpty()) return modelArrayList;
        modelArrayList.sort((o1, o2) -> {
            switch (sortBy) {
                case SORT_BY_BUCKET_NAME:
                    return o1.bucketName.compareTo(o2.bucketName) * sortDirection;
                case SORT_BY_DATE_DURATION:
                    return o1.duration.compareTo(o2.duration) * sortDirection;
                case SORT_BY_DATE_TAKEN:
                    return o1.dateTaken.compareTo(o2.dateTaken) * sortDirection;
                default:
                    return 0;
            }
        });
        return modelArrayList;
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
    }

    public void unregisterListener(MediaContentObserver.OnMediaUpdateListener listener) {
        listeners.remove(listener);
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

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.MediaColumns.BUCKET_ID + " ASC"
        );
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String bucketName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String dateTaken = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    String dateAdded = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));

                    mediaList.add(new MediaModel(bucketName, bucketID, data, dateTaken, dateAdded, duration));
                }
            } finally {
                cursor.close();
            }
        }
        return mediaList;
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
