package com.example.gallery.activities.album;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.dialog.AlbumPickerActivity;
import com.example.gallery.component.dialog.BottomDialogAddAlbum;
import com.example.gallery.component.dialog.BottomDialogRenameAlbum;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.TrashManager;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;

public class AlbumsFragment extends Fragment implements AlbumFrameAdapter.AlbumFrameListener, DatabaseObserver {
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    boolean viewMode;
    LinearLayout bottomSheet;
    AlbumFrameAdapter albumFrameAdapter;
    MainActivity mainActivity;

    RecyclerView recyclerView;
    private ActivityResultLauncher<Intent> createAlbumLauncher;
    private ActivityResultLauncher<Intent> renameAlbumLauncher;

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
        Observer<ArrayList<Integer>> selectedAlbumObserver = selectedAlbum -> {
            // Updates UI in here
            if (selectedAlbum.size() == 1) onShowBottomSheet();
            else {
                selectedAlbum.clear();
                onHideBottomSheet();
            }
            Log.d("PicturesFragment", "Selected media observer called with " + selectedAlbum.size() + " items");
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
    private void setUpBottomSheet() {
        TransitionSet transitionSet = new TransitionSet()
                .addTransition(new Slide())
                .setDuration(400);

        TransitionManager.beginDelayedTransition(bottomSheet, transitionSet);
        Button renameBtn = bottomSheet.findViewById(R.id.renameBtn);
        renameBtn.setOnClickListener(v -> {
            new BottomDialogRenameAlbum((albumName) -> {
                ArrayList<Integer> selectedPositions = albumViewModel.getSelectedAlbums().getValue();
                if (selectedPositions != null) {
                    if(albumName != null) {
                        // Album name typed in
                        AlbumModel albumModelSelect = albumViewModel.getAlbum(selectedPositions.get(0));
                        Intent intent = new Intent(mainActivity, AlbumManager.class);
                        intent.putExtra("action", "rename");
                        intent.putExtra("albumName", albumName);
                        intent.putExtra("album_model", albumModelSelect);

                        renameAlbumLauncher.launch(intent);
                    }
                }
            }).show(getParentFragmentManager(), "rename_album");
        });

    }
    private void refreshAlbums() {
        //getFromDatabase(); // Làm mới dữ liệu album từ cơ sở dữ liệu
        albumFrameAdapter.notifyDataSetChanged(); // Cập nhật lại giao diện người dùng
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheet = requireView().findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        setUpBottomSheet();

        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        createAlbumLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                getFromDatabase();
                            }else if (result.getResultCode() == RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to create album", Toast.LENGTH_SHORT).show();
                            }
                        });
        renameAlbumLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK) {
                                refreshAlbums();
                                onHideBottomSheet();
                                albumViewModel.getSelectedAlbums().getValue().clear();
                            }else if (result.getResultCode() == RESULT_CANCELED) {
                                //Toast.makeText(getContext(), "Failed to create album", Toast.LENGTH_SHORT).show();
                                onHideBottomSheet();
                                albumViewModel.getSelectedAlbums().getValue().clear();
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

        recyclerView = view.findViewById(R.id.album_grid);
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
    private void onShowBottomSheet() {
        viewMode = false;
        mainActivity.setBottomNavigationViewVisibility(View.GONE);
        recyclerView.setPadding(0, 0, 0, bottomSheet.getHeight());
        requireView().post(() -> {
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    private void onHideBottomSheet() {
        viewMode = true;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        recyclerView.setPadding(0, 0, 0, 0);
        requireView().post(() -> {
            mainActivity.setBottomNavigationViewVisibility(View.VISIBLE);
        });
    }
}