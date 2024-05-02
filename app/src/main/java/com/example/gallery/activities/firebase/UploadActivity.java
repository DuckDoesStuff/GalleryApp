package com.example.gallery.activities.firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.gallery.R;
import com.example.gallery.utils.UploadService;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class UploadActivity extends AppCompatActivity {
    private final Semaphore semaphore = new Semaphore(5); // Limit the number of concurrent uploads
    private CircularProgressIndicator circularProgressIndicator;
    private ArrayList<MediaModel> selectedImages;
    private StorageReference userRoot;
    private int totalProgress;
    private int currentProgress = 0;
    private TextView progressText;
    private GalleryDB db;
    private FirebaseUser user;
    private Snackbar uploadSnackbar;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Intent serviceIntent = new Intent(this, UploadService.class);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }else {
            startForegroundService(serviceIntent);
        }

        ImageButton backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(v -> {
            finish();
        });


    }

    private void uploadMedia(String localPath) {
        // Acquire a permit from the semaphore
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }


        // To get the album and media name
        String[] parts = localPath.split("/");
        String storagePath = parts[parts.length - 2] + "/" + parts[parts.length - 1];
        StorageReference mediaRef = userRoot.child(storagePath);
        // Upload the file
        StorageMetadata storageMetadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();
        Uri file = Uri.fromFile(new File(localPath));
        UploadTask uploadTask = mediaRef.putFile(file, storageMetadata);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Handle successful uploads
            semaphore.release();
            currentProgress++;
            circularProgressIndicator.setProgress((currentProgress / totalProgress) * 100);
            try {
//                db.onRemoveImageToUpload(localPath);
                // Update firestore
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                CollectionReference userCollection = fs.collection(user.getUid());
                mediaRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.onNewMedia(uri.toString());
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setLocalPath(localPath)
                            .setCloudPath(uri.toString());
                    userCollection.add(mediaModel);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                String text = "Uploading " + currentProgress + "/" + totalProgress;
                progressText.setText(text);
            });
        }).addOnFailureListener(e -> {
            // Handle failed uploads

            semaphore.release();
        }).addOnProgressListener(snapshot -> {
            // Handle progress updates

        });
    }
}
