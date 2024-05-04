package com.example.gallery.activities.pictures;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.gallery.R;
import com.example.gallery.utils.database.MediaModel;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    AppCompatActivity activity;
    ArrayList<MediaModel> images;
    MediaViewModel mediaViewModel;

    public ViewPagerAdapter(ArrayList<MediaModel> images, AppCompatActivity activity) {
        this.images = images;
        this.activity = activity;
    }

    public ViewPagerAdapter(AppCompatActivity activity, int sortType) {
        this.activity = activity;
        mediaViewModel = new ViewModelProvider(activity).get(MediaViewModel.class);
        this.images = mediaViewModel.getMedia().getValue();

        Observer<ArrayList<MediaModel>> mediaObserver = mediaModels -> {
            switch (sortType) {
                case PicutresFragment.SORT_LATEST:
                    mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
                    break;
                case PicutresFragment.SORT_OLDEST:
                    mediaModels.sort(Comparator.comparingLong(o -> o.dateTaken));
                    break;
                case PicutresFragment.SORT_ALBUM_NAME:
                    mediaModels.sort(Comparator.comparing(o -> o.albumName));
                    break;

            }
            this.images = mediaModels;
            Log.d("ViewPagerAdapter", "Media observer called with " + mediaModels.size() + " items");
            notifyDataSetChanged();
        };
        mediaViewModel.getMedia().observe(activity, mediaObserver);
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
        holder.bind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewPagerViewHolder holder) {
        super.onViewRecycled(holder);
//        if (holder.player != null) {
//            holder.player.release();
//            Log.d("Player", "View recycled and player released");
//        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
//        if (holder.player != null) {
//            holder.setupPlayerView();
//            Log.d("Player", "View attached and player prepared");
//        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewPagerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
//        if (holder.player != null) {
//            holder.player.release();
//            Log.d("Player", "View detached and player released");
//        }
    }

    public class ViewPagerViewHolder extends RecyclerView.ViewHolder {
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
        public void bind(MediaModel mediaModel) {
            this.mediaModel = mediaModel;
            // If media is an image
            if (mediaModel.type.contains("image")) {
                // First inflate mediaView with the ImageView
                touchImageView = new TouchImageView(itemView.getContext());
                touchImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                frameLayout.removeAllViews();
                frameLayout.addView(touchImageView);

                Glide.with(itemView)
                        .asBitmap()
                        .load(new File(mediaModel.localPath))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        touchImageView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

                touchImageView.setOnTouchListener((view, event) -> {
                    boolean result = true;
                    if (event.getPointerCount() >= 2 ||
                        view.canScrollHorizontally(1) &&
                        view.canScrollHorizontally(-1)) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN ||
                            event.getAction() == MotionEvent.ACTION_MOVE) {
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
                Log.d("ViewPagerAdapter", "Media type is video local path: " + mediaModel.localPath);
                setupPlayerView();
            }
        }

        public void setupPlayerView() {
            playerView = new PlayerView(activity);
            player = new ExoPlayer.Builder(activity).build();

            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(new File(mediaModel.localPath)));
            player.setMediaItem(mediaItem);
            player.prepare();
            playerView.setPlayer(player);

            frameLayout.removeAllViews();
            frameLayout.addView(playerView);
        }
    }
}
