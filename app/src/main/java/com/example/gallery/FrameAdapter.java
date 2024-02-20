package com.example.gallery;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameViewHolder> {
    private Context context;
    private int imgCount;
    private int imgSize;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private OnItemClickListener onClickCallBack;

    public FrameAdapter(Context context, int imgCount, int imgSize, OnItemClickListener onClickCallback) {
        this.context = context;
        this.imgCount = imgCount;
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
    }


    @NonNull
    @Override
    public FrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_frame, parent, false);

        return new FrameViewHolder(view, onClickCallBack);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
    }

    @Override
    public int getItemCount() {
        return imgCount;
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public FrameViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int pos = getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                    Toast.makeText(imageView.getContext(), "Image clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
