package com.example.gallery.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.AlbumActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.AlbumFrameAdapter;
import com.example.gallery.component.dialog.BottomDialog;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements AlbumFrameAdapter.AlbumFrameListener, DatabaseObserver {
    boolean viewMode;
    AlbumFrameAdapter albumFrameAdapter;
    MainActivity mainActivity;
    private ArrayList<AlbumModel> albums;
    private ArrayList<AlbumFrameAdapter.AlbumFrameModel> selectedAlbums;

    public AlbumsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onDatabaseChanged() {
        try (GalleryDB db = new GalleryDB(this.requireContext())) {
            albums = db.getAllAlbums();
            Log.d("AlbumsFragment", "Albums fragment got updated");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("AlbumsFragment", "Error getting albums");
        }
        mainActivity.runOnUiThread(() -> albumFrameAdapter.initFrameModels(albums));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedAlbums = new ArrayList<>();
        viewMode = true;
        mainActivity = ((MainActivity) requireActivity());

        try (GalleryDB db = new GalleryDB(this.requireContext())) {
            albums = db.getAllAlbums();
            GalleryDB.addAlbumObserver(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("AlbumsFragment", "Error getting albums");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GalleryDB.removeAlbumObserver(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton newAlbumButton = view.findViewById(R.id.plus);
        newAlbumButton.setOnClickListener(v -> {
            // Show dialog to create new album
            new BottomDialog(null).show(getParentFragmentManager(), "bottom_dialog");
        });

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int albSize = screenWidth / spanCount;

        RecyclerView recyclerView = view.findViewById(R.id.album_grid);
        albumFrameAdapter = new AlbumFrameAdapter(this, albums);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(albumFrameAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        mainActivity = ((MainActivity) requireActivity());


        return view;
    }

    @Override
    public void onItemClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemClick(position);
        ArrayList<MediaModel> mediaList = new ArrayList<>();

        // Get media in the same bucket here
        try (GalleryDB db = new GalleryDB(this.requireContext())) {
            mediaList = db.getMediaInAlbum(albums.get(position));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("AlbumsFragment", "Error getting media in album");
        }

        Intent intent = new Intent(getContext(), AlbumActivity.class);
        intent.putExtra("images", mediaList);
        intent.putExtra("name", albums.get(position).albumName);
        intent.putExtra("initial", position);
        mainActivity.startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemLongClick(position);
    }
}