package com.example.gallery;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.gallery.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPermission();

        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new PicutresFragment());
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
    }

    public void setPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For API level 33+
            String[] permissions = new String[] {
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };

            if(checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(permissions, 1);
            }
        }else {
            // For API level 29 (Pixel 7 Pro)
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission is denied, show user something to convince them
            }
        }
    }
}