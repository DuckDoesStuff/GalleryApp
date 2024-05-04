package com.example.gallery.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;
import java.util.Objects;

public class FaceActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {
    boolean viewMode = true;
    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;
    private Observer<ArrayList<Integer>> selectedMediaObserver;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        ArrayList<MediaModel> mediaModels;
        setContentView(R.layout.activity_face);
        if (intent != null) {
            mediaModels = intent.getParcelableArrayListExtra("mediaModels");
        } else {
            mediaModels = new ArrayList<>();
        }


        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        mediaViewModel.getMedia().setValue(mediaModels);



        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());



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
