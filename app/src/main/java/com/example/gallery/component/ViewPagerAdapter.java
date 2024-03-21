package com.example.gallery.component;

import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;

import java.util.ArrayList;


public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {
    ArrayList<String> images;
    ImageActivity imageActivity;
    public ViewPagerAdapter(ArrayList<String> images, ImageActivity imageActivity) {
        this.images = images;
        this.imageActivity = imageActivity;
    }

    @NonNull
    @Override
    public ViewPagerAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_page,parent,false);

        return new ViewPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewPagerViewHolder holder, int position) {
        String image = images.get(position);
        Glide.with(holder.itemView).load(image).centerCrop().into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {

        public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            float scaleFactor = 1.0f;

            public void setScaleFactor(float scaleFactor) {
                this.scaleFactor = scaleFactor;
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));
                imageView.setScaleX(scaleFactor);
                imageView.setScaleY(scaleFactor);
            }

            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                setScaleFactor(scaleFactor);
                return true;
            }

        }

        ImageView imageView;
        ScaleGestureDetector scaleGestureDetector;
        public ScaleListener scaleListener;
        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.page_image);
            scaleListener = new ScaleListener();
            scaleGestureDetector = new ScaleGestureDetector(itemView.getContext(), scaleListener);
            itemView.setOnTouchListener((v, event) -> {
                v.performClick();
                return scaleGestureDetector.onTouchEvent(event);
            });
        }
    }
}
