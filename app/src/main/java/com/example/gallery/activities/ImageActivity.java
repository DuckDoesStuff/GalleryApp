package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.gallery.R;
import com.example.gallery.component.ViewPagerAdapter;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    ArrayList<String> images;
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
        imageButton.setOnClickListener(v -> finish());


        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(images);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setOffscreenPageLimit(5);
        viewPager2.setCurrentItem(position, false);

        ImageButton editButton = findViewById(R.id.edit_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thực hiện chuyển sang EditActivity
                String currentImagePath = images.get(viewPager2.getCurrentItem());

                // Thực hiện chuyển sang EditActivity và truyền đường dẫn của hình ảnh
                Intent editIntent = new Intent(ImageActivity.this, EditActivity.class);
                editIntent.putExtra("imagePath", currentImagePath);
                startActivity(editIntent);
            }
        });

    }

}