package com.example.gallery.activities.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
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

public class UploadChooserActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {
    private Button uploadBtn;

    private MediaViewModel mediaViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());

        Intent intent = getIntent();
        ArrayList<MediaModel> mediaModels;
        if (intent != null) {
            mediaModels = intent.getParcelableArrayListExtra("mediaToUpload");
            mediaViewModel.getMedia().setValue(mediaModels);
        } else {
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
            ArrayList<Integer> selectedMedia = mediaViewModel.getSelectedMedia().getValue();
            if (selectedMedia == null || selectedMedia.isEmpty()) {
                Log.d("UploadChooserActivity", "Didn't select any media");
                finish();
                return;
            }
            ArrayList<MediaModel> selectedMediaModels = new ArrayList<>();
            for (int i : selectedMedia) {
                selectedMediaModels.add(mediaViewModel.getMedia(i));
            }
            uploadIntent.putParcelableArrayListExtra("selectedMedia", selectedMediaModels);
            startActivity(uploadIntent);
            finish();
        });

        Observer<ArrayList<Integer>> selectedMediaObserver = selectedMedia -> uploadBtn.setEnabled(selectedMedia != null && !selectedMedia.isEmpty());

        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);

        RecyclerView recyclerView = findViewById(R.id.media_picker_recycler_view);
        ImageFrameAdapter imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }
}
