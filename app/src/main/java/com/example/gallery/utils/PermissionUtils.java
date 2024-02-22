package com.example.gallery.utils;

import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionUtils {

    public interface PermissionCallback {
        // Runs once the user press "Allow"
        void onGranted();

        // Runs once the user press "Deny", we then show some UI to "convince" the user
        void onDenied();

        void showRationale();
    }


    /*
    * Use this to check and request for permission, require PermissionCallback to be implemented
    */
    public static void requestPermission(Fragment fragment, String permission, PermissionCallback callback) {
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, calling onGranted();
            callback.onGranted();
        } else if (fragment.shouldShowRequestPermissionRationale(permission)) {
            // Show rationale if needed
            // (You can include an educational UI here)
            callback.showRationale();
        } else {
            // Request permission
            ActivityResultLauncher<String> launcher = fragment.registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            callback.onGranted();
                        } else {
                            callback.onDenied();
                        }
                    });

            launcher.launch(permission);
        }
    }

    public static void requestMultiplePermissions(Fragment fragment, String[] permissions, PermissionCallback callback) {
        //TO DO: Implement this plz
    }
}

