package com.example.gallery.utils.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.R;
import com.example.gallery.utils.GalleryDB;
import com.example.gallery.utils.MediaModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class UploadActivity extends AppCompatActivity {
    private CircularProgressIndicator circularProgressIndicator;
    private ArrayList<MediaModel> selectedImages;
    private FirebaseStorage storage;
    private StorageReference userRoot;
    private final Semaphore semaphore = new Semaphore(5); // Limit the number of concurrent uploads
    private int totalProgress;
    private int currentProgress = 0;
    private TextView progressText;
    private GalleryDB db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        db = new GalleryDB(this);

        Intent intent = getIntent();
        if(intent == null) {
            finish();
            return;
        }
        selectedImages = intent.getParcelableArrayListExtra("selectedImages");
        if (user == null || selectedImages == null) {
            finish();
            return;
        }

        totalProgress = selectedImages.size();

        storage = FirebaseStorage.getInstance();
        String userFolder = user.getUid();
        userRoot = storage.getReference(userFolder);

        circularProgressIndicator = findViewById(R.id.progressIndicator);
        circularProgressIndicator.setVisibility(CircularProgressIndicator.VISIBLE);

        progressText = findViewById(R.id.progressText);
        progressText.setText("Uploading 0/" + totalProgress);

        Button doneButton = findViewById(R.id.doneButton);
        doneButton.setEnabled(false);
        doneButton.setOnClickListener(v -> finish());
        ImageButton backBtn = findViewById(R.id.backButton);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            for (MediaModel mediaModel : selectedImages) {
                uploadMedia(mediaModel.path);
            }

            handler.post(() -> {
                doneButton.setEnabled(true);
                backBtn.setVisibility(ImageButton.INVISIBLE);
                backBtn.setEnabled(false);
            });
        });

        backBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Upload")
                .setMessage("You sure you want to cancel the upload?")
                .setPositiveButton("OK", (dialog, which) -> {
                    executorService.shutdownNow();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
        });


        super.onCreate(savedInstanceState);
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
            circularProgressIndicator.setProgress((currentProgress / totalProgress * 100));
            try {
                db.onRemoveImageToUpload(localPath);
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
