package com.example.gallery.activities.album;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.activities.pictures.ImageActivity;
import com.example.gallery.activities.pictures.ImageFrameAdapter;
import com.example.gallery.activities.pictures.MediaViewModel;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {
    RecyclerView recyclerView;
    ImageFrameAdapter imageFrameAdapter;

    boolean viewMode = true;
    MainActivity mainActivity;

    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;
    private Observer<ArrayList<Integer>> selectedMediaObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        ImageButton backBtn = findViewById(R.id.back_button_album);
        backBtn.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        ArrayList<MediaModel> mediaModels;
        if (intent != null) {
            mediaModels = intent.getParcelableArrayListExtra("mediaModels");
        } else {
            mediaModels = new ArrayList<>();
        }

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        mediaViewModel.getMedia().setValue(mediaModels);
        mediaObserver = media -> {
            // Updates UI in here
            Log.d("AlbumActivity", "Media observer called with " + media.size() + " items");
        };
        mediaViewModel.getMedia().observe(this, mediaObserver);
        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());
        selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
            // Add bottom sheet controller here
            Log.d("AlbumActivity", "Selected media observer called with " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);

        TextView albumName = findViewById(R.id.album_name);
        albumName.setText(intent != null ? intent.getStringExtra("name") : "");

        recyclerView = findViewById(R.id.photo_album_grid);
        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        if (imageFrameAdapter == null)
            imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("mediaModels", mediaViewModel.getMedia().getValue());
        intent.putExtra("initial", position);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        ImageFrameAdapter.ImageFrameListener.super.onItemLongClick(position);
    }
}
