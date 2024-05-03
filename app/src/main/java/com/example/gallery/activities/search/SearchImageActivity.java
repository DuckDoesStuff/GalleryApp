package com.example.gallery.activities.search;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

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

public class SearchImageActivity extends AppCompatActivity implements ImageFrameAdapter.ImageFrameListener {

    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_view);
        RecyclerView recyclerView = findViewById(R.id.search_view);

        int spanCount = 3;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth/spanCount;
        EditText searchInput = findViewById(R.id.search_edit_text);
        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        ImageButton searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v->{
            String searchInputText = searchInput.getText().toString();
            if(searchInputText==null)
                return;

            try (GalleryDB db = new GalleryDB(this)) {
                ArrayList<MediaModel> trash = db.getSearchMedia(searchInputText);
                mediaViewModel.getMedia().setValue(trash);
                Log.d("SearchActivity", "Trash size: " + trash.size());
            } catch (Exception e) {
                Log.e("SearchActivity", "Error getting trash", e);
                mediaViewModel.getMedia().setValue(new ArrayList<>());
            }
            mediaObserver = mediaModels -> {
                // Do things
                Log.d("TrashActivity", "Media observer called with: " + mediaModels.size() + " items");
            };
            mediaViewModel.getMedia().observe(this, mediaObserver);
            ImageFrameAdapter imageFrameAdapter = new ImageFrameAdapter(imgSize,this,this);
            recyclerView.setAdapter(imageFrameAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this,spanCount));
        });



        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v->finish());

    }
}