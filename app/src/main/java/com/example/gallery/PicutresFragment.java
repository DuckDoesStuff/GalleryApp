package com.example.gallery;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;

import com.example.gallery.component.ImageFrameAdapter;
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
    private ArrayList<String> images;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

        ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if(isGranted) {
                loadImages();
            }
        });

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            loadImages();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected, and what
            // features are disabled if it's declined. In this UI, include a
            // "cancel" or "no thanks" button that lets the user continue
            // using your app without granting the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE);
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