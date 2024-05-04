package com.example.gallery.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.gallery.R;
import com.example.gallery.activities.pictures.ImageFrameAdapter;
import com.example.gallery.utils.database.DatabaseObserver;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.File;

public class HomeFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener, DatabaseObserver {
    File[] images;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        File faceDirectory = new File(android.os.Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DCIM), ".face");
//        if (faceDirectory.exists()) {
//            images = faceDirectory.listFiles();
//            if (images != null && images.length > 0) {
//
//                }
//            }
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDatabaseChanged() {

    }
}