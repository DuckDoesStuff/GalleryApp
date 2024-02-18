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
import com.example.gallery.component.ImageFrame;

public class FrameAdapter extends BaseAdapter {

    private Context mContext;
    private int mItemCount;


    public FrameAdapter(Context context, int num) {
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
        ImageFrame imageFrame;
        if (convertView == null) {
            imageFrame = new ImageFrame(mContext);
            imageFrame.setLayoutParams(new GridView.LayoutParams(200, 200)); // adjust size as needed
        } else {
            imageFrame = (ImageFrame) convertView;
        }
        return imageFrame;
    }
}
