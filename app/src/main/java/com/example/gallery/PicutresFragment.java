package com.example.gallery;

import android.Manifest;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.component.ImageFrameAdapter;
import com.example.gallery.utils.PermissionUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicutresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicutresFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
    public PicutresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * param1 Parameter 1.
     * param2 Parameter 2.
     * @return A new instance of fragment PicutresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PicutresFragment newInstance() {
        PicutresFragment fragment = new PicutresFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<String> images;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }



        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    new PermissionUtils.PermissionCallback() {
                        @Override
                        public void onGranted() {
                            // Access granted, load images
                            loadImages();
                        }

                        @Override
                        public void onDenied() {
                            // Do nothing for now
                        }

                        @Override
                        public void showRationale() {
                            // Do nothing for now
                        }
                    });
        }
        else {
            PermissionUtils.requestPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    new PermissionUtils.PermissionCallback() {
                        @Override
                        public void onGranted() {
                            // Access granted, load images
                            loadImages();
                        }

                        @Override
                        public void onDenied() {
                            // Do nothing for now
                        }

                        @Override
                        public void showRationale() {
                            // Do nothing for now
                        }
                    });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picutres, container, false);

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        RecyclerView recyclerView = view.findViewById(R.id.photo_grid);
        ImageFrameAdapter imageFrameAdapter = new ImageFrameAdapter(getContext(), imgSize, images, this);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerView.setAdapter(imageFrameAdapter);
        ImageButton dropdownButton = view.findViewById(R.id.settings);
        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if(item.getItemId() == R.id.choice1) {
                    return true;
                }else if (item.getItemId() == R.id.choice2) {
                    return true;
                }else if (item.getItemId() == R.id.choice3) {
                    return true;}

                return true;

            });

            popupMenu.show();
        });

        return view;
    }

    @Override
    public void onItemClick(int position) {
        Snackbar.make(requireContext(), requireView(), "Image clicked at " + position, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemLongClick(int position) {
        Snackbar.make(requireContext(), requireView(), "Image long clicked at " + position, Snackbar.LENGTH_SHORT).show();
    }

    public void loadImages() {
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

        for(String path : images) {
            Log.d("Media", path);
        }
    }
}