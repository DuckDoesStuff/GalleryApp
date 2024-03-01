package com.example.gallery;

import android.Manifest;
import android.animation.LayoutTransition;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.example.gallery.databinding.ActivityMainBinding;
import com.example.gallery.utils.PermissionUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;


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
    private void replaceFragment(Fragment fragment){
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