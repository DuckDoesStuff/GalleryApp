package com.example.gallery.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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

import java.util.ArrayList;

public class TrashFrameAdapter extends RecyclerView.Adapter<TrashFrameAdapter.FrameViewHolder> {
    private final int imgSize;
    private final TrashFrameListener onClickCallBack;
    public boolean selectionModeEnabled = false;
    private ArrayList<FrameModel> frameModels;
    private ArrayList<String> selectedImages;

    public TrashFrameAdapter(Context context, int imgSize, ArrayList<Integer> selectedPositions, ArrayList<String> images, ArrayList<String> selectedImages, TrashFrameListener onClickCallback) {
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
        this.selectedImages = selectedImages;
        initFrameModels(images);
    }
    public void initFrameModels(ArrayList<String> images) {
        if (images == null || images.isEmpty()) {
            frameModels = new ArrayList<>();
            // TODO: Remember to remove this
        } else {
            frameModels = new ArrayList<>();
            for (String file:images) {
                frameModels.add(new TrashFrameAdapter.FrameModel(file));

            }
        }
    }

    @NonNull
    @Override
    public TrashFrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Check if a recycled view holder is available
        FrameViewHolder holder;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
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
    public void onBindViewHolder(@NonNull TrashFrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
        FrameModel frameModel = frameModels.get(position);
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

    @Override
    public int getItemCount() {
        return frameModels.size();
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

        FrameModel frameModel;

        public FrameViewHolder(View itemView, TrashFrameListener listener) {
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
            Log.d("hehe", "toi glide");
            Glide.with(itemView).load(frameModel.file)
                    .transition(DrawableTransitionOptions
                            .withCrossFade(200))
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop().into(imageView);
        }
    }
    private static class FrameModel {
        private final String file;
        private boolean isSelected;

        public FrameModel(String file) {
            this.file = file;
            isSelected = false;

        }
    }
}