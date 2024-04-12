package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.component.TrashFrameAdapter;

import java.util.ArrayList;

public class TrashActivity extends AppCompatActivity implements TrashFrameAdapter.TrashFrameListener{
    ArrayList<String> images;
    RecyclerView recyclerView;
    TrashFrameAdapter trashFrameAdapter;

    boolean viewMode = true;
    MainActivity mainActivity;

    private ArrayList<String> selectedImages;
    private ArrayList<Integer> selectedPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Intent intent = getIntent();
        ImageButton backBtn = findViewById(R.id.back_button_trash);
        backBtn.setOnClickListener(v -> finish());


        final int position;
        if (intent != null) {
            images = intent.getStringArrayListExtra("imagesPath");

            position = intent.getIntExtra("initial", 0);
        } else {
            images = new ArrayList<>();
            position = -1;
        }

        recyclerView = findViewById(R.id.photo_album_grid);
        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        if (trashFrameAdapter == null)
            trashFrameAdapter = new TrashFrameAdapter(this, imgSize, selectedPositions, images, selectedImages, this);

        recyclerView.setAdapter(trashFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


}
