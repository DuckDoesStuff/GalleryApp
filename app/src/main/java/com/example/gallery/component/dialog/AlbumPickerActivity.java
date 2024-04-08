package com.example.gallery.component.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.GalleryDB;
import com.example.gallery.utils.MediaFetch;

import java.util.ArrayList;

public class AlbumPickerActivity extends AppCompatActivity implements AlbumPickerAdapter.AlbumPickerListener {
    ArrayList<MediaFetch.MediaModel> images;
    ArrayList<GalleryDB.AlbumScheme> albums;
    private GalleryDB db;


    public AlbumPickerActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_album_picker);

        Intent intent = getIntent();
        if (intent != null) {
            images = intent.getParcelableArrayListExtra("images");
        }else {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        ImageButton back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> finish());

        db = new GalleryDB(this);
        albums = db.getAlbums();

        RecyclerView recyclerView = findViewById(R.id.album_list_picker);
        AlbumPickerAdapter adapter = new AlbumPickerAdapter(albums, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    @Override
    public void onAlbumSelected(GalleryDB.AlbumScheme album) {
        new Thread(() -> {
            for (MediaFetch.MediaModel image : images) {
                AlbumManager.moveMedia(this, image.data, album.albumPath);
            }
            try {
                GalleryDB db = new GalleryDB(this);
                ArrayList<GalleryDB.AlbumScheme> temp = new ArrayList<>();
                temp.add(album);
                db.updateAlbums(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        finish();
    }
}
