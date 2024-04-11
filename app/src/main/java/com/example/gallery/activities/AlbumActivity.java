package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.component.ImageFrameAdapter;
import com.example.gallery.utils.MediaModel;

import java.util.ArrayList;

public class AlbumActivity extends  AppCompatActivity implements ImageFrameAdapter.ImageFrameListener{
    ArrayList<MediaModel> images;
    RecyclerView recyclerView;
    ImageFrameAdapter imageFrameAdapter;

    boolean viewMode = true;
    MainActivity mainActivity;

    private ArrayList<MediaModel> selectedImages;
    private ArrayList<Integer> selectedPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        ImageButton backBtn = findViewById(R.id.back_button_album);
        backBtn.setOnClickListener(v -> finish());

        TextView albumName = findViewById(R.id.album_name);
        albumName.setText(intent.getStringExtra("name"));

        final int position;
        if (intent != null) {
            images = intent.getParcelableArrayListExtra("images");
            position = intent.getIntExtra("initial", 0);
        } else {
            images = new ArrayList<>();
            position = -1;
        }

        recyclerView = findViewById(R.id.photo_album_grid);
        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        if (imageFrameAdapter == null)
            imageFrameAdapter = new ImageFrameAdapter(this, imgSize, selectedPositions, images, selectedImages, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onItemClick(int position) {
        ImageFrameAdapter.ImageFrameListener.super.onItemClick(position);
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("images", images);
        intent.putExtra("initial", position);
        this.startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        ImageFrameAdapter.ImageFrameListener.super.onItemLongClick(position);
    }
}
