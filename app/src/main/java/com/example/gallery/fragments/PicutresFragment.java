package com.example.gallery.fragments;

import static android.app.Activity.RESULT_OK;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.ImageFrameAdapter;
import com.example.gallery.utils.MediaContentObserver;
import com.example.gallery.utils.MediaFetch;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class PicutresFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener, MediaContentObserver.OnMediaUpdateListener {

    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    LinearLayout bottomSheet;
    boolean viewMode = true;
    MainActivity mainActivity;
    ImageFrameAdapter imageFrameAdapter;
    RecyclerView recyclerView;
    private ArrayList<MediaFetch.MediaModel> images;
    private ArrayList<MediaFetch.MediaModel> selectedImages;
    private ArrayList<Integer> selectedPositions;

    public PicutresFragment() {
        // Required empty public constructor
    }

    public static PicutresFragment newInstance() {
        return new PicutresFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(selectedImages == null)
            selectedImages = new ArrayList<>();

        if(selectedPositions == null)
            selectedPositions = new ArrayList<>();

        MediaFetch.getInstance(null).registerListener(this);
        MediaFetch.getInstance(null).fetchMedia(false);

        Log.d("Debug", "on create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaFetch.getInstance(null).unregisterListener(this);
    }

    @Override
    public void onMediaUpdate(ArrayList<MediaFetch.MediaModel> modelArrayList) {
        // Ensure running on UI thread
        images = modelArrayList;
        MediaFetch.sortArrayListModel(images, MediaFetch.SORT_BY_BUCKET_NAME, MediaFetch.SORT_DESC);
        requireActivity().runOnUiThread(() -> {
            imageFrameAdapter.selectionModeEnabled = false;
            imageFrameAdapter.initFrameModels(images);
            Log.d("Debug", "on media update of picture fragment");
        });
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
                if (item.getItemId() == R.id.choice1) {
                    Snackbar.make(requireView(), "Total images: " + selectedImages.size(), Snackbar.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.choice2) {
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
            imageFrameAdapter = new ImageFrameAdapter(getContext(), imgSize, selectedPositions, images, selectedImages, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        NestedScrollView scrollView = view.findViewById(R.id.scroll_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //If scrolled to the top
                if (!recyclerView.canScrollVertically(-1)) {
                    scrollView.scrollBy(0, -1);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    scrollView.scrollBy(0, dy);
                }
            }
        });
    }

    private void setUpBottomSheet() {
        Button deleteBtn = bottomSheet.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(v -> {
            MediaFetch.deleteMediaFiles(requireActivity().getContentResolver(), selectedImages);
        });
    }

    public void onDeleteResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            selectedImages.clear();
            onHideBottomSheet();
            imageFrameAdapter.selectionModeEnabled = false;
//            imageFrameAdapter.notifyDataSetChanged();
            Log.d("Debug", "Deleted images");
            // There is a bug in here hiding but I can't produce it consistently :(
        }
    }

    @Override
    public void onItemClick(int position) {
        // Hide bottom sheet if not selecting any images
        if (selectedImages.isEmpty() && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            onHideBottomSheet();
        }
        else if (viewMode) {
            Intent intent = new Intent(getContext(), ImageActivity.class);
            intent.putExtra("images", images);
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