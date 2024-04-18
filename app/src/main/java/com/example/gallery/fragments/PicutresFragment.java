package com.example.gallery.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.example.gallery.activities.ImageActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.activities.TrashActivity;
import com.example.gallery.component.ImageFrameAdapter;
import com.example.gallery.component.dialog.AlbumPickerActivity;
import com.example.gallery.component.MediaViewModel;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.TrashManager;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;

public class PicutresFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener, DatabaseObserver {

    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    LinearLayout bottomSheet;
    boolean viewMode = true;
    MainActivity mainActivity;
    ImageFrameAdapter imageFrameAdapter;
    RecyclerView recyclerView;
    private ArrayList<MediaModel> selectedImages;

    ActivityResultLauncher<Intent> albumManagerLauncher;

    ActivityResultLauncher<Intent> albumPickerLauncher;


    private MediaViewModel mediaViewModel;
    private Observer<ArrayList<MediaModel>> mediaObserver;
    private Observer<ArrayList<MediaModel>> selectedMediaObserver;

    public PicutresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDatabaseChanged() {
        getFromDatabase();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();
        if (selectedImages == null)
            selectedImages = new ArrayList<>();
        GalleryDB.addAlbumObserver(this);

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        mediaObserver = mediaModels -> {
            // Updates UI in here
            Log.d("PicturesFragment", "Media observer called with " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(this, mediaObserver);


        selectedMediaObserver = mediaModels -> {
            // Updates UI in here
        };
        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GalleryDB.removeMediaObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picutres, container, false);
        mainActivity = ((MainActivity) requireActivity());
        recyclerView = view.findViewById(R.id.photo_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);


        ImageButton dropdownButton = view.findViewById(R.id.settings);

        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if (item.getItemId() == R.id.trash) {
                    ArrayList<String> intentIn = TrashManager.getFilesFromTrash();
                    Intent intent = new Intent(getContext(), TrashActivity.class);
                    intent.putStringArrayListExtra("imagesPath", intentIn);
                    mainActivity.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.choice2) {
                    Log.d("PicturesFragment", "Selected size: " + selectedImages.size());
                    return true;
                } else if (item.getItemId() == R.id.choice3) {
                    return true;
                }

                return true;

            });

            popupMenu.show();
        });

        ImageButton searchButton = view.findViewById(R.id.search);
        searchButton.setOnClickListener(v -> {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                mainActivity.replaceFragment(new SearchViewFragment());
            }
        });

        return view;
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

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        if (imageFrameAdapter == null)
            imageFrameAdapter = new ImageFrameAdapter(imgSize, mediaViewModel, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        albumManagerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                selectedImages.clear();
                                imageFrameAdapter.selectionModeEnabled = false;
                                imageFrameAdapter.notifyDataSetChanged();
                                onHideBottomSheet();
                                Toast.makeText(getContext(), "Media added to album", Toast.LENGTH_SHORT).show();
                            }
                        });

        albumPickerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent resultData = result.getData();
                                if (resultData != null) {
                                    AlbumModel pickedAlbum = resultData.getParcelableExtra("album");
                                    if (pickedAlbum != null) {
                                        Log.d("PicturesFragment", "Starting AlbumManager");
                                        Intent newIntent = new Intent(mainActivity, AlbumManager.class);
                                        newIntent.putExtra("mediaModels", selectedImages);
                                        newIntent.putExtra("albumModel", pickedAlbum);
                                        newIntent.putExtra("action", "add");
                                        albumManagerLauncher.launch(newIntent);
                                    }
                                }
                            }
                        });
    }

    private void getFromDatabase() {
        try(GalleryDB db = new GalleryDB(getContext())) {
            ArrayList<MediaModel> mediaModels = db.getAllMedia();
            mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
            mediaViewModel.getMedia().setValue(mediaModels);
            Log.d("PicturesFragment", "Pictures fragment got updated");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PicturesFragment", "Error getting media from database");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getFromDatabase();
    }

    private void setUpBottomSheet() {
        Button deleteBtn = bottomSheet.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            imageFrameAdapter.selectionModeEnabled = false;
            imageFrameAdapter.notifyDataSetChanged();
            onHideBottomSheet();
            new Thread(() -> {
                for (MediaModel image : selectedImages) {
                    TrashManager.moveToTrash(requireContext(), image.localPath);
                }
                selectedImages.clear();
            }).start();

        });

        Button addBtn = bottomSheet.findViewById(R.id.addToBtn);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, AlbumPickerActivity.class);
            intent.putParcelableArrayListExtra("mediaModels", mediaViewModel.getSelectedMedia().getValue());
            albumPickerLauncher.launch(intent);
        });
    }

    @Override
    public void onItemClick(int position) {
        // Hide bottom sheet if not selecting any images
        if (!imageFrameAdapter.selectionModeEnabled && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            onHideBottomSheet();
        } else if (viewMode) {
            Intent intent = new Intent(getContext(), ImageActivity.class);
            intent.putExtra("mediaModels", mediaViewModel.getMedia().getValue());
            intent.putExtra("initial", position);
            mainActivity.startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(int position) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            onShowBottomSheet();
        }
    }

    private void onShowBottomSheet() {
        viewMode = false;
        mainActivity.setBottomNavigationViewVisibility(View.GONE);
        requireView().post(() -> {
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    private void onHideBottomSheet() {
        viewMode = true;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        requireView().post(() -> {
            mainActivity.setBottomNavigationViewVisibility(View.VISIBLE);
        });
    }
}