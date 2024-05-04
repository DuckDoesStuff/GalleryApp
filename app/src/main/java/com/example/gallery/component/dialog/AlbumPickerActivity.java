package com.example.gallery.component.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class AlbumPickerActivity extends AppCompatActivity implements AlbumPickerAdapter.AlbumPickerListener {
    ArrayList<MediaModel> mediaModels;
    ArrayList<AlbumModel> albums;
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
            mediaModels = intent.getParcelableArrayListExtra("mediaModels");
        } else {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        if (Objects.requireNonNull(mediaModels).isEmpty()) {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        ImageButton back = findViewById(R.id.back_button);
        back.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        db = new GalleryDB(this);
        albums = db.getAllAlbums();
        albums.sort(Comparator.comparing(a -> a.albumName));

        RecyclerView recyclerView = findViewById(R.id.album_list_picker);
        AlbumPickerAdapter adapter = new AlbumPickerAdapter(albums, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    }

    @Override
    public void onAlbumSelected(AlbumModel album) {
        // pass the selected album to the previous activity
        Intent intent = new Intent();
        intent.putExtra("album", album);
        setResult(RESULT_OK, intent);
        finish();
    }
}
