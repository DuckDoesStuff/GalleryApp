package com.example.gallery.utils;

import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Map;

public class PermissionUtils {

    // Request code to use in requesting permissions in activity
    public static int READ_EXTERNAL_STORAGE = 1;
    public static int READ_MEDIA_STORAGE = 1;

    public static void requestActivityPermission(AppCompatActivity activity, String permission, PermissionCallback callback, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, proceed to create the fragment
            callback.onGranted();
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},
                    requestCode);
        }
    }

    /*
     * Use these to check and request for permission, require PermissionCallback to be implemented
     */

    public static void requestMultipleActivityPermissions(AppCompatActivity activity, String[] permissions, PermissionCallback callback, int requestCode) {
        // Check if all permissions are already granted
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // All permissions granted, calling onGranted();
            callback.onGranted();
        } else {
            // Request permissions
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    public static void requestFragmentPermission(Fragment fragment, String permission, PermissionCallback callback) {
        ActivityResultLauncher<String> launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        callback.onGranted();

                    } else {
                        callback.onDenied();
                    }

                });
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, calling onGranted();
            callback.onGranted();
        } else if (fragment.shouldShowRequestPermissionRationale(permission)) {
            // Show rationale if needed
            // (You can include an educational UI here)
            callback.showRationale();
            launcher.launch(permission);
        } else {
            // Request permission
            launcher.launch(permission);
        }
    }

    public static void requestMultipleFragmentPermissions(Fragment fragment, String[] permissions, PermissionCallback callback) {
        ActivityResultLauncher<String[]> launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), permissionsResult -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : permissionsResult.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        callback.onGranted();
                    } else {
                        callback.onDenied();
                    }
                });

        // Check if all permissions are already granted
        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (allPermissionsGranted) {
            // All permissions granted, calling onGranted();
            callback.onGranted();
        } else {
            // Request permissions
            launcher.launch(permissions);
        }
    }

    public interface PermissionCallback {
        // Runs once the user press "Allow"
        void onGranted();

        // Runs once the user press "Deny", we then show some UI to "convince" the user
        default void onDenied() {
            Log.d("PermissionUtils", "Permission denied");
        }

        default void showRationale() {
            Log.d("PermissionUtils", "Show rationale called");
        }
    }
}

