package com.example.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {

    private Context mContext;
    private int mItemCount;

    public FrameAdapter(Context context, int itemCount) {
        mContext = context;
        mItemCount = itemCount;
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_frame, parent, false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {

        public FrameViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
