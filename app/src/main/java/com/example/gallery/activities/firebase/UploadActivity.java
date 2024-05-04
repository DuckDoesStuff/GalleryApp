package com.example.gallery.activities.firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.gallery.R;
import com.example.gallery.utils.UploadService;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Intent serviceIntent = new Intent(this, UploadService.class);
                startForegroundService(serviceIntent);
            } else {
                // Permission denied
                // Show a message to the user
                new MaterialAlertDialogBuilder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Android requires you to turn on notification for background upload.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Close the activity
                        finish();
                    })
                    .show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("selectedMedia");
        if (mediaModels == null || mediaModels.isEmpty()) {
            finish();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }else {
            Log.d("UploadActivity", "Starting upload service");
            Intent serviceIntent = new Intent(this, UploadService.class);
            serviceIntent.putParcelableArrayListExtra("selectedMedia", mediaModels);
            startForegroundService(serviceIntent);
        }

        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            finish();
        });


    }
}
