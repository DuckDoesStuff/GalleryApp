package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gallery.R;
import com.example.gallery.component.ViewPagerAdapter;
import com.example.gallery.utils.MediaFetch;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    ArrayList<MediaFetch.MediaModel> images;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        final int position;
        if (intent != null) {
            images = intent.getParcelableArrayListExtra("images");
            position = intent.getIntExtra("initial", 0);
        } else {
            images = new ArrayList<>();
            position = -1;
        }
        ImageButton imageButton = findViewById(R.id.back_button);
        imageButton.setOnClickListener(v -> finish());

        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(images, this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setCurrentItem(position, false);
        viewPager2.setDrawingCacheEnabled(true);
        viewPager2.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
    }

    public void setViewPagerInputEnabled(boolean enabled) {
        viewPager2.setUserInputEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Debug", "Image activity gone");
    }
}