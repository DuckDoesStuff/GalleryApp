package com.example.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.component.AlbumFrame;

public class AlbumFrameAdapter extends BaseAdapter {

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
