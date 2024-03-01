package com.example.gallery.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
    public boolean selectionModeEnabled = false;

    public interface ImageFrameListener {
        default void onItemClick(int position) {}
        default void onItemLongClick(int position) {}
    }

    private class FrameModel {
        private String filePath;
        private boolean isSelected;

        public FrameModel(String filePath) {
            this.filePath = filePath;
            isSelected = false;
        }
    }

    private ArrayList<FrameModel> frameModels;
    private ArrayList<String> selectedImages;

    private final ImageFrameListener onClickCallBack;

    public ImageFrameAdapter(Context context, int imgSize, ArrayList<String> images, ArrayList<String> selectedImages, ImageFrameListener onClickCallback) {
        this.context = context;
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
        this.selectedImages = selectedImages;

        if (images == null || images.isEmpty()) {
            frameModels = new ArrayList<>();
            // Remember to remove this
            Toast.makeText(context, "No images", Toast.LENGTH_SHORT).show();
        }else {
            frameModels = new ArrayList<>();
            for(String image:images) {
                frameModels.add(new FrameModel(image));
            }
        }

    }


    @NonNull
    @Override
    public ImageFrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Check if a recycled view holder is available
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        FrameViewHolder holder;
        if (itemView.getTag() != null) {
            holder = (FrameViewHolder) itemView.getTag();
        } else {
            // Create a new view holder if none is available
            holder = new FrameViewHolder(itemView, onClickCallBack);
            itemView.setTag(holder);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
        FrameModel frameModel = frameModels.get(position);
        holder.bind(frameModel);

        holder.checkBox.setVisibility(selectionModeEnabled ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if(selectionModeEnabled) {
                frameModel.isSelected = !frameModel.isSelected;
                holder.checkBox.setChecked(frameModel.isSelected);



                if(frameModel.isSelected)
                {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f); // Scale down RGB values to reduce brightness

                    // Create a ColorMatrixColorFilter with the brightness reduction ColorMatrix
                    ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

                    // Apply the color filter to the ImageView to reduce brightness
                    holder.imageView.setColorFilter(colorFilter);
                    selectedImages.add(frameModel.filePath);}
                else{
                    holder.imageView.clearColorFilter();
                    selectedImages.remove(frameModel.filePath);}

                // Turn off selection mode if not selecting any images
                if(selectedImages.isEmpty()) {
                    selectionModeEnabled = false;
                    notifyDataSetChanged();
                }
            }
            onClickCallBack.onItemClick(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            selectionModeEnabled = true;
            frameModel.isSelected = true;

            selectedImages.add(frameModel.filePath);
            onClickCallBack.onItemLongClick(position);

            notifyDataSetChanged();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return frameModels.size();
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;

        FrameModel frameModel;
        public FrameViewHolder(View itemView, ImageFrameListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            checkBox = itemView.findViewById(R.id.select_box);

            Glide.with(itemView).load(new ColorDrawable(Color.GRAY)).centerCrop().into(imageView);
        }

        public void bind(FrameModel frameModel) {
            this.frameModel = frameModel;
            checkBox.setChecked(frameModel.isSelected);
            if (frameModel.isSelected) {
                // Tạo một ColorMatrix để giảm độ sáng của hình ảnh
                ColorMatrix colorMatrix = new ColorMatrix();
                colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f); // Scale down RGB values to reduce brightness

                // Create a ColorMatrixColorFilter with the brightness reduction ColorMatrix
                ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

                // Apply the color filter to the ImageView to reduce brightness
                imageView.setColorFilter(colorFilter);
            } else {
                // Nếu không được chọn, hiển thị hình ảnh bình thường bằng cách xóa bỏ color filter
                imageView.clearColorFilter();
            }
            Glide.with(itemView).load(frameModel.filePath)
                    .transition(DrawableTransitionOptions
                            .withCrossFade(200))
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop().into(imageView);
        }
    }
}
