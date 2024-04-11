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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gallery.R;
import com.example.gallery.utils.MediaFetch;

import java.io.File;
import java.util.ArrayList;

public class TrashFrameAdapter extends RecyclerView.Adapter<TrashFrameAdapter.FrameViewHolder> {
    private final int imgSize;
    private final TrashFrameAdapter.TrashFrameListener onClickCallBack;
    public boolean selectionModeEnabled = false;
    private ArrayList<TrashFrameAdapter.FrameModel> frameModels;

    private ArrayList<File> images;
    private ArrayList<File> selectedImages;

    public TrashFrameAdapter(Context context, int imgSize, File[] images, File[] selectedImages, TrashFrameAdapter.TrashFrameListener onClickCallback) {
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
        initFrameModels(images);
        for (File file:selectedImages) {
            this.selectedImages.add(file);
        }
    }
    public void initFrameModels(File[] images) {
        if (images == null || images.length == 0) {
            frameModels = new ArrayList<>();
            // TODO: Remember to remove this
        } else {
            frameModels = new ArrayList<>();
            for (File file:images) {
                frameModels.add(new TrashFrameAdapter.FrameModel(file));
                this.images.add(file);
            }
        }
    }



    @Override
    public void onBindViewHolder(@NonNull TrashFrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
        TrashFrameAdapter.FrameModel frameModel = frameModels.get(position);
        holder.bind(frameModel);
        holder.checkBox.setVisibility(selectionModeEnabled ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (selectionModeEnabled) {
                frameModel.isSelected = !frameModel.isSelected;
                holder.checkBox.setChecked(frameModel.isSelected);

                if (frameModel.isSelected) {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f); // Scale down RGB values to reduce brightness

                    // Create a ColorMatrixColorFilter with the brightness reduction ColorMatrix
                    ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

                    // Apply the color filter to the ImageView to reduce brightness
                    holder.imageView.setColorFilter(colorFilter);
                    selectedImages.add(frameModel.file);
                } else {
                    holder.imageView.clearColorFilter();
                    selectedImages.remove(frameModel.file);
                }

                // Turn off selection mode if not selecting any images
                if (selectedImages.isEmpty()) {
                    selectionModeEnabled = false;
                }
            }
            onClickCallBack.onItemClick(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            selectionModeEnabled = true;
            frameModel.isSelected = true;

            selectedImages.add(frameModel.file);
            onClickCallBack.onItemLongClick(position);

            return true;
        });
    }

    @NonNull
    @Override
    public TrashFrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Check if a recycled view holder is available
        TrashFrameAdapter.FrameViewHolder holder;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        if (itemView.getTag() != null) {
            holder = (TrashFrameAdapter.FrameViewHolder) itemView.getTag();
        } else {
            // Create a new view holder if none is available
            holder = new TrashFrameAdapter.FrameViewHolder(itemView, onClickCallBack);
            itemView.setTag(holder);
        }
        return holder;
    }


    @Override
    public int getItemCount() {
        return 0;
    }

    public interface TrashFrameListener {
        default void onItemClick(int position) {
        }

        default void onItemLongClick(int position) {
        }
    }

    static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;

        TrashFrameAdapter.FrameModel frameModel;

        public FrameViewHolder(View itemView, TrashFrameAdapter.TrashFrameListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            checkBox = itemView.findViewById(R.id.select_box);

            Glide.with(itemView).load(new ColorDrawable(Color.GRAY)).centerCrop().into(imageView);
        }
        public void bind(TrashFrameAdapter.FrameModel frameModel) {
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
            Glide.with(itemView).load(frameModel.file)
                    .transition(DrawableTransitionOptions
                            .withCrossFade(200))
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop().into(imageView);
        }
    }
    private static class FrameModel {
        private final File file;
        private boolean isSelected;

        public FrameModel(File file) {
            this.file = file;
            isSelected = false;
        }
    }
}
