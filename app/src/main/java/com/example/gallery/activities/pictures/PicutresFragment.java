package com.example.gallery.activities.pictures;

import android.app.Activity;
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
import android.widget.PopupMenu;
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
import com.example.gallery.activities.FavoriteActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.activities.search.SearchImageActivity;
import com.example.gallery.activities.TrashActivity;
import com.example.gallery.component.dialog.AlbumPickerActivity;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.TrashManager;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
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
    ActivityResultLauncher<Intent> trashManagerLauncher;

    private MediaViewModel mediaViewModel;

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
        GalleryDB.addMediaObserver(this);

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        // Updates UI in here
        Observer<ArrayList<MediaModel>> mediaObserver = mediaModels -> {
            // Updates UI in here
            Log.d("PicturesFragment", "Media observer called with " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(this, mediaObserver);

        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());
        // Updates UI in here
        Observer<ArrayList<Integer>> selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
            if (!selectedMedia.isEmpty()) onShowBottomSheet();
            else onHideBottomSheet();
            Log.d("PicturesFragment", "Selected media observer called with " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);

        Log.d("PicturesFragment", "Initialized");
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


        ImageButton dropdownButton = view.findViewById(R.id.settings);
        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if (item.getItemId() == R.id.trash) {
                    Intent intent = new Intent(getContext(), TrashActivity.class);
                    mainActivity.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.favorite) {
                    Intent intent = new Intent(getContext(), FavoriteActivity.class);
                    mainActivity.startActivity(intent);
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
            Intent intent = new Intent(getContext(), SearchImageActivity.class);
            mainActivity.startActivity(intent);
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

        imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        albumManagerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            selectedImages.clear();
                            mediaViewModel.clearSelectedMedia();
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Toast.makeText(getContext(), "Media added to album", Toast.LENGTH_SHORT).show();
                            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to add media to album", Toast.LENGTH_SHORT).show();
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
                            }else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                selectedImages.clear();
                                mediaViewModel.clearSelectedMedia();
                            }
                        });

        trashManagerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            selectedImages.clear();
                            mediaViewModel.clearSelectedMedia();
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Toast.makeText(getContext(), "Media moved to trash", Toast.LENGTH_SHORT).show();
                            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to move media to trash", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void getFromDatabase() {
        try(GalleryDB db = new GalleryDB(getContext())) {
            ArrayList<MediaModel> mediaModels = db.getAllLocalMedia();
            mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
            mediaViewModel.getMedia().setValue(mediaModels);
            Log.d("PicturesFragment", "Pictures fragment got updated");
        } catch (Exception e) {
            Log.d("PicturesFragment", "Error getting media from database");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getFromDatabase();
    }

    private void setUpBottomSheet() {
        TransitionSet transitionSet = new TransitionSet()
                .addTransition(new Slide())
                .setDuration(400);

        TransitionManager.beginDelayedTransition(bottomSheet, transitionSet);

        Button trashBtn = bottomSheet.findViewById(R.id.trashBtn);
        trashBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, TrashManager.class);
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                selectedImages.clear();
                for (int position : selectedPositions) {
                    selectedImages.add(mediaViewModel.getMedia(position));
                }
            }
            intent.putParcelableArrayListExtra("mediaModels", selectedImages);
            intent.putExtra("action", "trash");
            trashManagerLauncher.launch(intent);
        });

        Button addBtn = bottomSheet.findViewById(R.id.addToBtn);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, AlbumPickerActivity.class);
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                selectedImages.clear();
                for (int position : selectedPositions) {
                    selectedImages.add(mediaViewModel.getMedia(position));
                }
            }
            intent.putParcelableArrayListExtra("mediaModels", selectedImages);
            albumPickerLauncher.launch(intent);
        });
        Button shareBtn = bottomSheet.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(v -> {
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                ArrayList<Uri> imageUris = new ArrayList<>();
                selectedImages.clear();
                Log.d("hii","share");
                for (int position : selectedPositions) {
                    // Use position to retrieve MediaModel from selectedImages list
                    selectedImages.add(mediaViewModel.getMedia(position));
                    MediaModel mediaModel = mediaViewModel.getMedia(position);
                    String imagePath = mediaModel.localPath;
                    File imageFile = new File(imagePath);
                    Uri imageUri = FileProvider.getUriForFile(requireContext(),
                            "com.example.gallery", imageFile);
                    imageUris.add(imageUri);
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("image/*");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Share Images"));
                mediaViewModel.clearSelectedMedia();
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        if (viewMode) {
            Intent intent = new Intent(mainActivity, ImageActivity.class);
            for (MediaModel media:mediaViewModel.getMedia().getValue()) {
                if (media.favorite) {
                    Log.d("favorite", "true");
                } else {
                    Log.d("favorite", "false");

                }
            }
            intent.putParcelableArrayListExtra("mediaModels", mediaViewModel.getMedia().getValue());
            intent.putExtra("initial", position);
            mainActivity.startActivity(intent);
        }
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