package com.example.gallery.component;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;
import com.example.gallery.utils.database.MediaModel;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.util.ArrayList;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    static ImageActivity imageActivity;
    ArrayList<MediaModel> images;

    public ViewPagerAdapter(ArrayList<MediaModel> images, ImageActivity imageActivity) {
        this.images = images;
        ViewPagerAdapter.imageActivity = imageActivity;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page, parent, false);

        return new ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewPagerViewHolder holder, int position) {
        holder.onBind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewPagerViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            holder.player.release();
            Log.d("Player", "View recycled and player released");
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.player != null) {
            holder.setupPlayerView();
            Log.d("Player", "View attached and player prepared");
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.player != null) {
            holder.player.release();
            Log.d("Player", "View detached and player released");
        }
    }

    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        FrameLayout frameLayout;
        TouchImageView touchImageView;
        PlayerView playerView;
        ExoPlayer player;
        MediaModel mediaModel;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.page_frame);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void onBind(MediaModel mediaModel) {
            this.mediaModel = mediaModel;

            // If media is an image
            if (mediaModel.type.contains("image")) {
                // First inflate mediaView with the ImageView
                touchImageView = new TouchImageView(itemView.getContext());
                touchImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                frameLayout.removeAllViews();
                frameLayout.addView(touchImageView);

                Glide.with(itemView).asBitmap().load(new File(mediaModel.localPath)).diskCacheStrategy(DiskCacheStrategy.ALL).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        touchImageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

                //                touchImageView.setImageURI(Uri.fromFile(new File(mediaModel.localPath)));

                touchImageView.setOnTouchListener((view, event) -> {
                    boolean result = true;
                    if (event.getPointerCount() >= 2 || view.canScrollHorizontally(1) && view.canScrollHorizontally(-1)) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                            // Disallow RecyclerView to intercept touch events.
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            result = false;
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            // Allow RecyclerView to intercept touch events.
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                    return result;
                });

            } else {
                setupPlayerView();
            }
        }

        public void setupPlayerView() {
            playerView = new PlayerView(imageActivity);
            player = new ExoPlayer.Builder(imageActivity).build();

            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(mediaModel.localPath)));
            player.setMediaItem(mediaItem);
            player.prepare();
            playerView.setPlayer(player);

            frameLayout.removeAllViews();
            frameLayout.addView(playerView);
        }
    }
}
