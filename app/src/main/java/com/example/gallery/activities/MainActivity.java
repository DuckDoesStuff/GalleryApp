package com.example.gallery.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.gallery.R;
import com.example.gallery.activities.album.AlbumsFragment;
import com.example.gallery.activities.firebase.GuestFragment;
import com.example.gallery.activities.firebase.UserFragment;
import com.example.gallery.activities.firebase.UserViewModel;
import com.example.gallery.activities.pictures.PicutresFragment;
import com.example.gallery.databinding.ActivityMainBinding;
import com.example.gallery.fragments.HomeFragment;
import com.example.gallery.utils.MediaStoreService;
import com.example.gallery.utils.PermissionUtils;
import com.example.gallery.utils.database.DatabaseQuery;
import com.example.gallery.utils.database.GalleryDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    public UserViewModel userViewModel;
    ActivityMainBinding binding;
    Fragment currentFragment;
    PicutresFragment picutresFragment;
    HomeFragment homeFragment;
    AlbumsFragment albumsFragment;
    Fragment profileFragment;
    Observer<FirebaseUser> userObserver;
    FirebaseUser user;

    DatabaseQuery databaseQuery;

    PermissionUtils.PermissionCallback permissionCallback = () -> {
        Intent serviceIntent = new Intent(this, MediaStoreService.class);
        startService(serviceIntent);

        new Thread(this::startDatabase).start();

        picutresFragment = new PicutresFragment();
        replaceFragment(picutresFragment);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!Environment.isExternalStorageManager()) {
                Uri uri = Uri.parse("package:" + getApplicationContext().getPackageName());

                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(uri);

                startActivity(intent);
            }
            PermissionUtils.requestMultipleActivityPermissions(
                    this,
                    new String[]
                            {
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO
                            },
                    permissionCallback,
                    1
            );
        } else {
            PermissionUtils.requestMultipleActivityPermissions(
                    this,
                    new String[]
                            {
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            },
                    permissionCallback,
                    1
            );
        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userObserver = firebaseUser -> {
            BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
            user = firebaseUser;
            if (bottomNavigationView.getSelectedItemId() == R.id.profile) {
                if (firebaseUser != null) {
                    profileFragment = new UserFragment();
                } else
                    profileFragment = new GuestFragment();
                replaceFragment(profileFragment);
            }
        };
        userViewModel.getCurrentUser().observe(this, userObserver);
        user = FirebaseAuth.getInstance().getCurrentUser();

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                replaceFragment(homeFragment);
            } else if (itemId == R.id.pictures) {
                if (picutresFragment == null) {
                    picutresFragment = new PicutresFragment();
                }
                replaceFragment(picutresFragment);
            } else if (itemId == R.id.albums) {
                if (albumsFragment == null) {
                    albumsFragment = new AlbumsFragment();
                }
                replaceFragment(albumsFragment);
            } else if (itemId == R.id.profile) {
                if (user != null) {
                    profileFragment = new UserFragment();
                } else {
                    profileFragment = new GuestFragment();
                }
                replaceFragment(profileFragment);
            }
            return true;
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
        currentFragment = fragment;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // All permissions granted, calling onGranted();
                permissionCallback.onGranted();
            } else {
                // Permissions denied
                // You may want to show a message or take other actions here
            }
        }
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

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void startDatabase() {
        databaseQuery = new DatabaseQuery(this);

        // Query the MediaStore for new media here
        databaseQuery.queryMedia();
        databaseQuery.queryAlbum();
        // Update SQLite database with the retrieved information here
        databaseQuery.addToMediaTable(databaseQuery.mediaModelsToAdd);
        databaseQuery.addToAlbumTable(databaseQuery.albumModelsToAdd);
        databaseQuery.removeFromMediaTable(databaseQuery.mediaModelsToDelete);
        databaseQuery.removeFromAlbumTable(databaseQuery.albumModelsToDelete);
        // Sync with firestore if authenticated


        GalleryDB.notifyMediaObservers();
        GalleryDB.notifyAlbumObservers();
        Log.d("MainActivity", "Database refreshed");
    }
}