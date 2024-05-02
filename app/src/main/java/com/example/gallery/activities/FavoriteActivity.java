package com.example.gallery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.pictures.ImageFrameAdapter;
import com.example.gallery.activities.pictures.MediaViewModel;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;
import java.util.Objects;

public class FavoriteActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {
    boolean viewMode = true;
    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;
    private Observer<ArrayList<Integer>> selectedMediaObserver;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        try (GalleryDB db = new GalleryDB(this)) {
            ArrayList<MediaModel> favorite = db.getAllFavorite();
            mediaViewModel.getMedia().setValue(favorite);
            Log.d("FavoriteActivity", "Favorite size: " + favorite.size());
        } catch (Exception e) {
            Log.e("FavoriteActivity", "Error getting favorite", e);
            mediaViewModel.getMedia().setValue(new ArrayList<>());
        }

        mediaObserver = mediaModels -> {
            // Do things
            Log.d("TrashActivity", "Media observer called with: " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(this, mediaObserver);

        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());
        selectedMediaObserver = selectedMedia -> {
            // Do things
            viewMode = selectedMedia.isEmpty();
            Log.d("TrashActivity", "Selected media observer called with: " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);


        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());

        TextView favoriteCount = findViewById(R.id.fav_count);
        favoriteCount.setText(Objects.requireNonNull(mediaViewModel.getMedia().getValue()).size() + " items");


        RecyclerView recyclerView = findViewById(R.id.fav_grid);

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        ImageFrameAdapter imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    @Override
    public void onItemClick(int position) {
        if (viewMode) {
            Intent intent = new Intent(this, TrashViewActivity.class);
            intent.putParcelableArrayListExtra("mediaModels", mediaViewModel.getMedia().getValue());
            intent.putExtra("initial", position);
            startActivity(intent);
        }
    }
}
