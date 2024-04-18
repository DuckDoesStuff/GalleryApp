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
import com.example.gallery.utils.TrashManager;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    ArrayList<MediaModel> mediaModels;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        final int position;
        if (intent != null) {
            mediaModels = intent.getParcelableArrayListExtra("mediaModels");
            position = intent.getIntExtra("initial", 0);
        } else {
            mediaModels = new ArrayList<>();
            position = -1;
        }
        ImageButton imageButton = findViewById(R.id.back_button);
        imageButton.setOnClickListener(v -> finish());

        ImageButton editButton = findViewById(R.id.edit_btn);

        ImageButton deleteButton = findViewById(R.id.trash_btn);
        deleteButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Delete this file")
                .setMessage("Are you sure to delete this file?")
                .setPositiveButton("OK", (dialog, which) -> {
                    int currentPosition = viewPager2.getCurrentItem();
                    mediaModels.remove(currentPosition);
                    viewPagerAdapter.notifyDataSetChanged();

                    if (mediaModels.isEmpty()) {
                        finish();
                    } else {
                        if (currentPosition < mediaModels.size()) {
                            viewPager2.setCurrentItem(currentPosition, false);
                        } else {
                            viewPager2.setCurrentItem(currentPosition - 1, false);
                        }
                    }

                    String currentImagePath = mediaModels.get(currentPosition).localPath;
                    TrashManager.moveToTrash(ImageActivity.this, currentImagePath);

                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Xử lý khi nhấn nút Cancel

                })
                .show();
        });

        editButton.setOnClickListener(v -> {
            String currentImagePath = mediaModels.get(viewPager2.getCurrentItem()).localPath;

            Intent editIntent = new Intent(ImageActivity.this, EditActivity.class);
            editIntent.putExtra("imagePath", currentImagePath);
            startActivity(editIntent);
        });

        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(mediaModels, this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setCurrentItem(position, false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Debug", "Image activity gone");
    }

}