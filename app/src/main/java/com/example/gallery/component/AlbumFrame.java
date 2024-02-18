package com.example.gallery.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gallery.R;

public class AlbumFrame extends FrameLayout {

    public AlbumFrame(@NonNull Context context) {
        super(context);
        applyStyle(context);
    }

    public AlbumFrame(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        applyStyle(context);
    }

    public AlbumFrame(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyStyle(context);
    }

    private void applyStyle(Context context) {
        LayoutInflater.from(context).inflate(R.layout.album_frame, this, true);
    }
}
