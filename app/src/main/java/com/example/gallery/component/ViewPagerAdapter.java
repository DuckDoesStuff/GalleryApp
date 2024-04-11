package com.example.gallery.component;

import android.animation.ObjectAnimator;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;
import com.example.gallery.utils.MediaModel;

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
        if(holder.player != null) {
            holder.player.release();
            Log.d("Player", "View recycled and player released");
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder.player != null) {
            holder.setupPlayerView();
            Log.d("Player", "View attached and player prepared");
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if(holder.player != null) {
            holder.player.release();
            Log.d("Player", "View detached and player released");
        }
    }

    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        final float minScale = 1.0f;
        final float maxScale = 5.0f;
        public ScaleListener scaleListener;
        public GestureListener gestureListener;
        float scaleFactor = 1.0f;
        boolean isScaling = false;
        PointF startPoint = new PointF();

        FrameLayout frameLayout;
        ImageView imageView;
        PlayerView playerView;
        ExoPlayer player;
        ScaleGestureDetector scaleGestureDetector;
        GestureDetector gestureDetector;
        MediaModel mediaModel;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            frameLayout = itemView.findViewById(R.id.page_frame);
        }

        public void onBind(MediaModel mediaModel) {
            this.mediaModel = mediaModel;

            // If media is an image
            if(mediaModel.duration == null) {
                // First inflate mediaView with the ImageView
                imageView = new ImageView(itemView.getContext());
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                frameLayout.removeAllViews();
                frameLayout.addView(imageView);

                Glide.with(itemView).load(mediaModel.path).into(imageView);

                scaleListener = new ScaleListener();
                scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), scaleListener);

                gestureListener = new GestureListener();
                gestureDetector = new GestureDetector(itemView.getContext(), gestureListener);

                itemView.setOnTouchListener((v, event) -> {
                    gestureDetector.onTouchEvent(event);
                    scaleGestureDetector.onTouchEvent(event);
                    if (!isScaling && imageView.getScaleX() != 1.0f && imageView.getScaleY() != 1.0f) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startPoint.set(event.getX(), event.getY());
                                break;
                            case MotionEvent.ACTION_MOVE:
                                float dx = event.getX() - startPoint.x;
                                float dy = event.getY() - startPoint.y;
                                updatePan(dx, dy);
                                startPoint.set(event.getX(), event.getY());
                                break;
                        }
                    }
                    v.performClick();
                    return true;
                });
            }else {
                setupPlayerView();
            }
        }
        public void setupPlayerView() {
            playerView = new PlayerView(imageActivity);
            player = new ExoPlayer.Builder(imageActivity).build();

            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(mediaModel.path)));
            player.setMediaItem(mediaItem);
            player.prepare();
            playerView.setPlayer(player);

            frameLayout.removeAllViews();
            frameLayout.addView(playerView);
        }

        public void setSmoothScaleFactor(float scaleFactor) {
            this.scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));

            // Smooth scaling
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_X, this.scaleFactor);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_Y, this.scaleFactor);

            // Animation duration
            scaleXAnimator.setDuration(200);
            scaleYAnimator.setDuration(200);

            scaleXAnimator.start();
            scaleYAnimator.start();

            imageActivity.setViewPagerInputEnabled(scaleFactor == 1.0f);
        }

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
            imageActivity.setViewPagerInputEnabled(scaleFactor == 1.0f);
            imageView.setScaleX(this.scaleFactor);
            imageView.setScaleY(this.scaleFactor);
        }

        private void updatePan(float dx, float dy) {
            Log.d("Pan", "Size: " + imageView.getWidth() + " " + imageView.getHeight());
            float currentTransX = imageView.getTranslationX();
            float currentTransY = imageView.getTranslationY();

            Log.d("Pan", "Before: " + imageView.getX() + " " + imageView.getY());


            float newTransX = Math.min(Math.abs(currentTransX + dx), imageView.getWidth()) * Math.signum(currentTransX + dx);
            float newTransY = Math.min(Math.abs(currentTransY + dy), imageView.getHeight()) * Math.signum(currentTransY + dy);


            imageView.setTranslationX(currentTransX + dx);
            imageView.setTranslationY(currentTransY + dy);
            Log.d("Pan", "After: " + imageView.getX() + " " + imageView.getY());
        }

        public class GestureListener extends GestureDetector.SimpleOnGestureListener {

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (scaleFactor == 1.0f) {
                    setSmoothScaleFactor(3.0f);
                } else {
                    setSmoothScaleFactor(1.0f);
                    imageView.setX(0);
                    imageView.setY(0);
                }

                return true;
            }
        }

        public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                isScaling = true;
                scaleFactor *= detector.getScaleFactor();
                setScaleFactor(scaleFactor);
                return true;
            }

            @Override
            public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
                super.onScaleEnd(detector);
                isScaling = false;
            }


        }
    }
}
