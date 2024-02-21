package com.example.gallery.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;

public class ImageFrameAdapter extends RecyclerView.Adapter<ImageFrameAdapter.FrameViewHolder> {
    private final Context context;
    private final int imgCount;
    private final int imgSize;

    public interface ImageFrameListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
    private final ImageFrameListener onClickCallBack;

    public ImageFrameAdapter(Context context, int imgCount, int imgSize, ImageFrameListener onClickCallback) {
        this.context = context;
        this.imgCount = imgCount;
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
    }


    @NonNull
    @Override
    public ImageFrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_frame, parent, false);

        return new FrameViewHolder(view, onClickCallBack);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
    }

    @Override
    public int getItemCount() {
        return imgCount;
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public FrameViewHolder(View itemView, ImageFrameListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);

            Glide.with(itemView).load(R.drawable.colorful).centerCrop().into(imageView);
            itemView.setOnClickListener(v -> {
                if(listener != null) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if(listener != null) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(pos);
                    }
                }
                return true;
            });
        }
    }
}
