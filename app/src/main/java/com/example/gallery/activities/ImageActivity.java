package com.example.gallery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gallery.R;
import com.example.gallery.component.ViewPagerAdapter;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    ArrayList<String> images;
    String currentImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        final int position;
        if(data != null) {
            images = data.getStringArrayList("images");
            position = data.getInt("initial");
        }
        else {
            images = new ArrayList<>();
            position = -1;
        }
        ImageButton imageButton = findViewById(R.id.back_button);
        imageButton.setOnClickListener(v -> {
            finish();
        });

        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(images);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setOffscreenPageLimit(5);
        viewPager2.setCurrentItem(position, false);
    }

}