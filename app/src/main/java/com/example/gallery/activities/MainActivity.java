package com.example.gallery.activities;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.gallery.fragments.AlbumsFragment;
import com.example.gallery.fragments.HomeFragment;
import com.example.gallery.fragments.PicutresFragment;
import com.example.gallery.fragments.ProfileFragment;
import com.example.gallery.R;
import com.example.gallery.databinding.ActivityMainBinding;
import com.example.gallery.utils.PermissionUtils;
import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Fragment currentFragment;
    PermissionUtils.PermissionCallback permissionCallback = () -> replaceFragment(new PicutresFragment());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.requestMultipleActivityPermissions(
                this,
                new String[] {android.Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO},
                permissionCallback,
                PermissionUtils.READ_MEDIA_STORAGE
            );
        }
        else {
            PermissionUtils.requestActivityPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                permissionCallback,
                PermissionUtils.READ_EXTERNAL_STORAGE
            );
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.home) {
                replaceFragment(new HomeFragment());
            }else if (itemId == R.id.pictures) {
                replaceFragment(new PicutresFragment());
            }else if (itemId == R.id.albums) {
                replaceFragment(new AlbumsFragment());
            }else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });

    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
        currentFragment = fragment;
    }


    public void setBottomNavigationViewVisibility(int visibility) {
//        TransitionSet transitionSet = new TransitionSet()
//                .addTransition(new Fade())
//                .addTransition(new ChangeBounds())
//                .setDuration(300);
//
//        TransitionManager.beginDelayedTransition(binding.bottomNavigationView, transitionSet);
        binding.bottomNavigationView.setVisibility(visibility);
    }
}