package com.example.gallery.component;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gallery.R;
import com.example.gallery.utils.database.MediaModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ImageFrameAdapter extends RecyclerView.Adapter<ImageFrameAdapter.FrameViewHolder> {
    private final int imgSize;
    private final ImageFrameListener onClickCallBack;
    public boolean selectionModeEnabled = false;
    public boolean canSelect = true;
    private ArrayList<FrameModel> frameModels;

    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;
    private Observer<ArrayList<MediaModel>> selectedMediaObserver;


    public ImageFrameAdapter(int imgSize, MediaViewModel mediaViewModel, ImageFrameListener onClickCallback) {
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
        this.mediaViewModel = mediaViewModel;
        frameModels = new ArrayList<>();

        mediaObserver = media -> {
            // Do things
            initFrameModels(media);
            Log.d("ImageFrameAdapter", "Media observer called with " + media.size() + " items");
        };
        mediaViewModel.getMedia().observeForever(mediaObserver);

        selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
        };
        mediaViewModel.getSelectedMedia().observeForever(selectedMediaObserver);
    }

    public void initFrameModels(ArrayList<MediaModel> images) {
        if (images == null || images.isEmpty()) {
            frameModels = new ArrayList<>();
        } else {
            frameModels = new ArrayList<>();
            for (MediaModel media : images) {
                frameModels.add(new FrameModel(media));
            }
        }
        Log.d("ImageFrameAdapter", "Frame models initialized with " + frameModels.size() + " items");
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ImageFrameAdapter.FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Check if a recycled view holder is available
        FrameViewHolder holder;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_frame, parent, false);
        if (itemView.getTag() != null) {
            holder = (FrameViewHolder) itemView.getTag();
        } else {
            // Create a new view holder if none is available
            holder = new FrameViewHolder(itemView, imgSize, onClickCallBack);
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

        if (frameModel.media.type.contains("video")) {
            holder.play.setVisibility(View.VISIBLE);
        } else {
            holder.play.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (selectionModeEnabled) {
                frameModel.isSelected = !frameModel.isSelected;
                holder.checkBox.setChecked(frameModel.isSelected);

                if (frameModel.isSelected) {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    // Scale down RGB values to reduce brightness
                    colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f);
                    ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

                    holder.imageView.setColorFilter(colorFilter);
//                    selectedImages.add(frameModel.media);
                } else {
                    holder.imageView.clearColorFilter();
//                    selectedImages.remove(frameModel.media);
                }

                // Turn off selection mode if not selecting any images
//                if (selectedImages.isEmpty()) {
//                    selectionModeEnabled = false;
//                    notifyDataSetChanged();
//                }
            }
            if (onClickCallBack != null)
                onClickCallBack.onItemClick(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            selectionModeEnabled = true;
            frameModel.isSelected = true;

//            selectedImages.add(frameModel.media);
            if (onClickCallBack != null)
                onClickCallBack.onItemLongClick(position);

            notifyDataSetChanged();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return frameModels.size();
    }

    public interface ImageFrameListener {
        default void onItemClick(int position) {
        }

        default void onItemLongClick(int position) {
        }
    }

    public static class FrameViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        ImageView play;

        FrameModel frameModel;

        int imgSize;

        public FrameViewHolder(View itemView, int imgSize, ImageFrameListener listener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            checkBox = itemView.findViewById(R.id.select_box);
            play = itemView.findViewById(R.id.play_button);
            this.imgSize = imgSize;
        }

        public void bind(FrameModel frameModel) {
            this.frameModel = frameModel;

            if (!Objects.equals(frameModel.media.localPath, "")) {
                itemView.findViewById(R.id.cloud_icon).setVisibility(View.INVISIBLE);
            }
            checkBox.setChecked(frameModel.isSelected);
            if (frameModel.isSelected) {
                ColorMatrix colorMatrix = new ColorMatrix();
                // Scale down RGB values to reduce brightness
                colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f);

                ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
                imageView.setColorFilter(colorFilter);
            } else {
                imageView.clearColorFilter();
            }


            Uri contentUri = ContentUris.withAppendedId(
                    frameModel.media.type.contains("video") ? android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI : android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    frameModel.media.mediaID);
            ContentResolver contentResolver = itemView.getContext().getContentResolver();
            try {
                Bitmap thumbnail = contentResolver.loadThumbnail(
                        contentUri, new Size(600, 600), null);

                Glide.with(itemView).load(thumbnail)
                        .transition(DrawableTransitionOptions
                                .withCrossFade(200))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(new ColorDrawable(Color.GRAY))
                        .centerCrop().into(imageView);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("ImageFrameAdapter", "Error loading thumbnail for " + frameModel.media.localPath + " with ID " + frameModel.media.mediaID + " and type " + frameModel.media.type);

                Glide.with(itemView).load(frameModel.media.localPath)
                        .transition(DrawableTransitionOptions
                                .withCrossFade(200))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(new ColorDrawable(Color.GRAY))
                        .centerCrop().into(imageView);
            }

        }
    }

    private static class FrameModel {
        private final MediaModel media;
        private boolean isSelected;

        public FrameModel(MediaModel media) {
            this.media = media;
            isSelected = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mediaViewModel.getMedia().removeObserver(mediaObserver);
        mediaViewModel.getSelectedMedia().removeObserver(selectedMediaObserver);
        Log.d("ImageFrameAdapter", "Finalized ImageFrameAdapter");
    }
}
