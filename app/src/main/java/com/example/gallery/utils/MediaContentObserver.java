package com.example.gallery.utils;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class MediaContentObserver extends ContentObserver {
    public MediaContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d("MediaObserve", "New media indexed, initiating fetch");
        try {
            MediaFetch.getInstance(null).fetchMedia(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnMediaUpdateListener {
        void onMediaUpdate(ArrayList<MediaFetch.MediaModel> modelArrayList);
    }
}