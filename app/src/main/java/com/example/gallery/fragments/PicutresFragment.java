package com.example.gallery.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.ImageActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.ImageFrameAdapter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicutresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicutresFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener {

    public PicutresFragment() {
        // Required empty public constructor
    }
    public static PicutresFragment newInstance() {
        PicutresFragment fragment = new PicutresFragment();
        return fragment;
    }

    private ArrayList<String> images;
    private ArrayList<String> selectedImages;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    LinearLayout bottomSheet;
    boolean viewMode;
    MainActivity mainActivity;
    ImageFrameAdapter imageFrameAdapter;
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedImages = new ArrayList<>();
        viewMode = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picutres, container, false);
        mainActivity = ((MainActivity) requireActivity());

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        recyclerView = view.findViewById(R.id.photo_grid);
        imageFrameAdapter = new ImageFrameAdapter(getContext(), imgSize, images, selectedImages, this);
        loadImages();

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerView.setAdapter(imageFrameAdapter);

        ImageButton dropdownButton = view.findViewById(R.id.settings);

        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if(item.getItemId() == R.id.choice1) {
                    Snackbar.make(requireView(), "Total images: " + selectedImages.size(), Snackbar.LENGTH_SHORT).show();
                    return true;
                }else if (item.getItemId() == R.id.choice2) {
                    return true;
                }else if (item.getItemId() == R.id.choice3) {
                    return true;
                }

                return true;

            });

            popupMenu.show();
        });

        ImageButton searchButton = view.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    mainActivity.replaceFragment(new SearchViewFragment());
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheet = requireView().findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onItemClick(int position) {
        if (viewMode) {
            Intent intent = new Intent(getContext(), ImageActivity.class);
            Bundle data = new Bundle();
            data.putStringArrayList("images", images);
            data.putInt("initial", position);
            intent.putExtras(data);
            mainActivity.startActivity(intent);
        }

        // Hide bottom sheet if not selecting any images
        if(selectedImages.isEmpty() && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            onHideBottomSheet();
            viewMode = true;
        }
    }

    @Override
    public void onItemLongClick(int position) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            onShowBottomSheet();
            viewMode = false;
        }
    }

    void onShowBottomSheet() {
        mainActivity.setBottomNavigationViewVisibility(View.GONE);
        new Handler().postDelayed(() -> {
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }, 100);
    }

    void onHideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        new Handler().postDelayed(() -> {
            mainActivity.setBottomNavigationViewVisibility(View.VISIBLE);
        }, 100);
    }

    public void loadImages() {
        Thread threadLoadImage = new Thread(new Runnable() {
            @Override
            public void run() {
                images = new ArrayList<>();

                // Choose which column to query
                String[] projection = new String[] {
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media.DATA,
                };

                Cursor cursor = requireContext().
                        getContentResolver().
                        query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                projection,
                                null,
                                null,
                                MediaStore.Images.Media.DATE_ADDED + " DESC");

                if (cursor != null) {
                    try {
                        while(cursor.moveToNext()) {
                            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                            images.add(imagePath);
                        }
                    } finally {
                        cursor.close();
                    }
                }
                imageFrameAdapter.initFrameModels(images);
                imageFrameAdapter.notifyDataSetChanged();
            }
        });
        threadLoadImage.start();
    }
}