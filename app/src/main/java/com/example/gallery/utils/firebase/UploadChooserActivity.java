package com.example.gallery.utils.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.component.ImageFrameAdapter;
import com.example.gallery.utils.MediaModel;

import java.util.ArrayList;

public class UploadChooserActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {
    private ArrayList<MediaModel> foundImages;
    private ArrayList<MediaModel> selectedImages;
    private Button uploadBtn;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        Intent intent = getIntent();
        if (intent != null) {
            foundImages = new ArrayList<>();
            foundImages = intent.getParcelableArrayListExtra("foundImages");
            selectedImages = new ArrayList<>();
        }
        else {
            finish();
        }

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> finish());


        uploadBtn = findViewById(R.id.uploadButton);
        uploadBtn.setEnabled(false);
        uploadBtn.setOnClickListener(v -> {
            Intent uploadIntent = new Intent(this, UploadActivity.class);
            uploadIntent.putParcelableArrayListExtra("selectedImages", selectedImages);
            startActivity(uploadIntent);
        });

        RecyclerView recyclerView = findViewById(R.id.media_picker_recycler_view);
        ImageFrameAdapter imageFrameAdapter = new ImageFrameAdapter(this, imgSize, foundImages, selectedImages, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    @Override
    public void onItemClick(int position) {
        uploadBtn.setEnabled(!selectedImages.isEmpty());
    }

    @Override
    public void onItemLongClick(int position) {
        uploadBtn.setEnabled(!selectedImages.isEmpty());
    }
}
