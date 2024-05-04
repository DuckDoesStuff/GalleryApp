package com.example.gallery.activities.pictures;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gallery.R;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;
import java.util.Objects;

public class ImageFrameAdapter extends RecyclerView.Adapter<ImageFrameAdapter.FrameViewHolder> {
    private final int imgSize;
    private final ImageFrameListener onClickCallBack;
    public boolean selectionModeEnabled = false;
    private final MediaViewModel mediaViewModel;
    int oldSize = 0;

    public ImageFrameAdapter(int imgSize, Fragment fragment, ImageFrameListener onClickCallback) {
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;
        mediaViewModel = new ViewModelProvider(fragment).get(MediaViewModel.class);

        Observer<ArrayList<MediaModel>> mediaObserver = mediaModels -> {
            // Do things
            notifyItemRangeChanged(0, oldSize);
            Log.d("ImageFrameAdapter", "Media observer called with " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(fragment.getViewLifecycleOwner(), mediaObserver);


        Observer<ArrayList<Integer>> selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
            selectionModeEnabled = !selectedMedia.isEmpty();
            Log.d("ImageFrameAdapter", "Selected media observer called with " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(fragment.getViewLifecycleOwner(), selectedMediaObserver);


        Log.d("ImageFrameAdapter", "Initialized");
    }

    public ImageFrameAdapter(int imgSize, AppCompatActivity activity, ImageFrameListener onClickCallback) {
        this.imgSize = imgSize;
        this.onClickCallBack = onClickCallback;

        mediaViewModel = new ViewModelProvider(activity).get(MediaViewModel.class);

        Observer<ArrayList<MediaModel>> mediaObserver = mediaModels -> {
            // Do things
            notifyItemRangeChanged(0, mediaModels.size());
            Log.d("ImageFrameAdapter", "Media observer called with " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(activity, mediaObserver);

        Observer<ArrayList<Integer>> selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
            selectionModeEnabled = !selectedMedia.isEmpty();
            Log.d("ImageFrameAdapter", "Selected media observer called with " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(activity, selectedMediaObserver);

        oldSize = Objects.requireNonNull(mediaViewModel.getMedia().getValue()).size();
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
            holder = new FrameViewHolder(itemView, imgSize, mediaViewModel);
            itemView.setTag(holder);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFrameAdapter.FrameViewHolder holder, int position) {
        holder.imageView.setLayoutParams(new LinearLayout.LayoutParams(imgSize, imgSize));
        MediaModel mediaModel = mediaViewModel.getMedia(position);

        holder.bind(mediaModel);

        if (mediaModel.type.contains("video")) {
            holder.play.setVisibility(View.VISIBLE);
        } else {
            holder.play.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (selectionModeEnabled) {
                mediaViewModel.setSelectedMedia(position);
            }
            else if (onClickCallBack != null)
                onClickCallBack.onItemClick(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            mediaViewModel.setSelectedMedia(position);

            if (onClickCallBack != null)
                onClickCallBack.onItemLongClick(position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return Objects.requireNonNull(mediaViewModel.getMedia().getValue()).size();
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

        MediaModel mediaModel;
        int imgSize;

        MediaViewModel mediaViewModel;
        Observer<ArrayList<Integer>> selectedMediaObserver;

        public FrameViewHolder(View itemView, int imgSize, MediaViewModel mediaViewModel) {
            super(itemView);
            imageView = itemView.findViewById(R.id.frame);
            checkBox = itemView.findViewById(R.id.select_box);
            play = itemView.findViewById(R.id.play_button);
            this.imgSize = imgSize;
            this.mediaViewModel = mediaViewModel;

            selectedMediaObserver =
                (selectedMedia) -> {
                    checkBox.setVisibility(!selectedMedia.isEmpty() ? View.VISIBLE : View.GONE);
                    int position = getAbsoluteAdapterPosition();
                    checkBox.setChecked(selectedMedia.contains(position));
                    if (mediaViewModel.isSelected(position)) {
                        ColorMatrix colorMatrix = new ColorMatrix();
                        colorMatrix.setScale(0.7f, 0.7f, 0.7f, 1.0f);
                        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
                        imageView.setColorFilter(colorFilter);
                    } else {
                        imageView.clearColorFilter();
                    }
                };
            mediaViewModel.getSelectedMedia().observeForever(selectedMediaObserver);
        }

        public void bind(MediaModel mediaModel) {
            this.mediaModel = mediaModel;

            if (!Objects.equals(mediaModel.localPath, "")) {
                itemView.findViewById(R.id.cloud_icon).setVisibility(View.INVISIBLE);
            }

            Glide.with(itemView).load(mediaModel.localPath)
                    .transition(DrawableTransitionOptions
                    .withCrossFade(200))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .centerCrop().into(imageView);

//            Uri contentUri = ContentUris.withAppendedId(
//                    mediaModel.type.contains("video") ? android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI : android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    mediaModel.mediaID);
//            ContentResolver contentResolver = itemView.getContext().getContentResolver();
//            try {
//                Bitmap thumbnail = contentResolver.loadThumbnail(
//                        contentUri, new Size(600, 600), null);
//
//                Glide.with(itemView).load(thumbnail)
//                        .transition(DrawableTransitionOptions
//                        .withCrossFade(200))
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(new ColorDrawable(Color.GRAY))
//                        .centerCrop().into(imageView);
//            } catch (IOException e) {
//                // e.printStackTrace();
//                Log.d("ImageFrameAdapter", "Error loading thumbnail for " + mediaModel.localPath + " with ID " + mediaModel.mediaID + " and type " + mediaModel.type);
//
//                Glide.with(itemView).load(mediaModel.localPath)
//                        .transition(DrawableTransitionOptions
//                        .withCrossFade(200))
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(new ColorDrawable(Color.GRAY))
//                        .centerCrop().into(imageView);
//            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("ImageFrameAdapter", "Finalized ImageFrameAdapter");
    }
}
