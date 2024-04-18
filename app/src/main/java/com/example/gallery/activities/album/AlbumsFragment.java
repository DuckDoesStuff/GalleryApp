package com.example.gallery.activities.album;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.dialog.BottomDialogAddAlbum;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements AlbumFrameAdapter.AlbumFrameListener, DatabaseObserver {
    boolean viewMode;
    AlbumFrameAdapter albumFrameAdapter;
    MainActivity mainActivity;
    private ActivityResultLauncher<Intent> createAlbumLauncher;

    private AlbumViewModel albumViewModel;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    private void getFromDatabase() {
        try (GalleryDB db = new GalleryDB(this.requireContext())) {
            ArrayList<AlbumModel> albums = db.getAllAlbums();
            albums.sort((a, b) -> a.albumName.compareTo(b.albumName));
            albumViewModel.getAlbums().setValue(albums);
            Log.d("AlbumsFragment", "Albums fragment got updated");
        } catch (Exception e) {
            Log.d("AlbumsFragment", "Error getting albums");
        }
    }


    @Override
    public void onDatabaseChanged() {
        getFromDatabase();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewMode = true;
        mainActivity = ((MainActivity) requireActivity());
        GalleryDB.addAlbumObserver(this);

        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        // Updates UI in here
        Observer<ArrayList<AlbumModel>> albumObserver = albums -> {
            // Updates UI in here
            Log.d("AlbumsFragment", "Address: " + albums.hashCode());
            Log.d("AlbumsFragment", "Album observer called with " + albums.size() + " items");
        };
        albumViewModel.getAlbums().observe(this, albumObserver);

        albumViewModel.getSelectedAlbums().setValue(new ArrayList<>());
        // Updates UI in here
        Observer<ArrayList<AlbumModel>> selectedAlbumObserver = selectedAlbum -> {
            // Updates UI in here
        };
        albumViewModel.getSelectedAlbums().observe(this, selectedAlbumObserver);

        Log.d("AlbumsFragment", "Initialized");
    }

    @Override
    public void onStart() {
        super.onStart();
        getFromDatabase();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GalleryDB.removeAlbumObserver(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createAlbumLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                getFromDatabase();
                            }else if (result.getResultCode() == RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to create album", Toast.LENGTH_SHORT).show();
                            }
                        });

        ImageButton newAlbumButton = view.findViewById(R.id.plus);
        newAlbumButton.setOnClickListener(v -> {
            // Show dialog to create new album
            new BottomDialogAddAlbum((albumName) -> {
                if(albumName != null) {
                    // Album name typed in
                    Intent intent = new Intent(mainActivity, AlbumManager.class);
                    intent.putExtra("action", "create");
                    intent.putExtra("albumName", albumName);
                    createAlbumLauncher.launch(intent);
                }
            }).show(getParentFragmentManager(), "add_album");
        });

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int albSize = screenWidth / spanCount;

        RecyclerView recyclerView = view.findViewById(R.id.album_grid);
        albumFrameAdapter = new AlbumFrameAdapter(this, this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(albumFrameAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onItemClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemClick(position);
        ArrayList<MediaModel> mediaList = new ArrayList<>();
        ArrayList<AlbumModel> albums = albumViewModel.getAlbums().getValue();
        if(albums == null) return;

        // Get media in the same bucket here
        try (GalleryDB db = new GalleryDB(this.requireContext())) {
            mediaList = db.getMediaInAlbum(albums.get(position));
        } catch (Exception e) {
            Log.d("AlbumsFragment", "Error getting media in album");
        }

        Intent intent = new Intent(getContext(), AlbumActivity.class);
        mediaList.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
        intent.putExtra("mediaModels", mediaList);
        intent.putExtra("name", albums.get(position).albumName);
        mainActivity.startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemLongClick(position);
    }
}