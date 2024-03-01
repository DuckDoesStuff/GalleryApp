package com.example.gallery.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    public static class AlbumFrameAdapter extends BaseAdapter {

        private Context mContext;
        private int mItemCount;


        public AlbumFrameAdapter(Context context, int num) {
            mContext = context;
            mItemCount = num;
        }

        @Override
        public int getCount() {
            return mItemCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AlbumFrame albumFrame;
            if (convertView == null) {
                albumFrame = new AlbumFrame(mContext);
    //            albumFrame.setLayoutParams(new GridView.LayoutParams(300, 300)); // adjust size as needed
            } else {
                albumFrame = (AlbumFrame) convertView;
            }
            return albumFrame;
        }
    }
}
