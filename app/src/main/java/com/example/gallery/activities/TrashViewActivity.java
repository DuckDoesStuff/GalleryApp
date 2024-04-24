package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gallery.R;
import com.example.gallery.activities.pictures.ViewPagerAdapter;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class TrashViewActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash_view);

        Intent intent = getIntent();
        if(intent == null) {
            finish();
            return;
        }

        ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("mediaModels");
        int position = intent.getIntExtra("initial", 0);
        if(mediaModels == null || mediaModels.isEmpty()) {
            finish();
            return;
        }

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> {
            finish();
        });

        ImageButton deleteBtn = findViewById(R.id.delete_button);
        ImageButton restoreBtn = findViewById(R.id.restore_button);

        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(mediaModels, this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setCurrentItem(position, false);
    }


}
