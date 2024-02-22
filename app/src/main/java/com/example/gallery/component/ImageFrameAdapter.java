package com.example.gallery.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gallery.R;

import java.util.ArrayList;

public class ImageFrameAdapter extends RecyclerView.Adapter<ImageFrameAdapter.FrameViewHolder> {
    private final Context context;
    private final int imgSize;

    public interface ImageFrameListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }
    private final ImageFrameListener onClickCallBack;
    private final ArrayList<String> images;

    public ImageFrameAdapter(Context context, int imgSize, ArrayList<String> images, ImageFrameListener onClickCallback) {
        this.context = context;
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;

        if (images == null || images.isEmpty()) {
            this.images = new ArrayList<>();
            // Remember to remove this
            Toast.makeText(context, "No images", Toast.LENGTH_SHORT).show();
        }else {
            this.images = images;
        }
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
        holder.bind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        String filePath;
        public FrameViewHolder(View itemView, ImageFrameListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            Glide.with(itemView).load(new ColorDrawable(Color.GRAY)).centerCrop().into(imageView);
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

        public void bind(String filePath) {
            this.filePath = filePath;
            Glide.with(itemView).load(filePath).transition(DrawableTransitionOptions.withCrossFade(200)).placeholder(new ColorDrawable(Color.GRAY)).centerCrop().into(imageView);
        }
    }
}
