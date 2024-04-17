package com.example.gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.component.TrashFrameAdapter;
import com.example.gallery.utils.TrashManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

public class TrashActivity extends AppCompatActivity implements TrashFrameAdapter.TrashFrameListener {
    ArrayList<String> images;
    RecyclerView recyclerView;
    TrashFrameAdapter trashFrameAdapter;

    boolean viewMode = true;
    MainActivity mainActivity;

    TextView restoreBtn;
    TextView deleteBtn;

    private ArrayList<String> selectedImages;
    private ArrayList<Integer> selectedPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Intent intent = getIntent();
        ImageButton backBtn = findViewById(R.id.back_button_trash);
        backBtn.setOnClickListener(v -> finish());
        restoreBtn = findViewById(R.id.edit_button);
        deleteBtn = findViewById(R.id.empty_button);
        restoreBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);


        final int position;
        if (intent != null) {
            images = intent.getStringArrayListExtra("imagesPath");

            position = intent.getIntExtra("initial", 0);
        } else {
            images = new ArrayList<>();
            position = -1;
        }

        recyclerView = findViewById(R.id.photo_album_grid);
        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;
        selectedImages = new ArrayList<>();
        if (trashFrameAdapter == null)
            trashFrameAdapter = new TrashFrameAdapter(this, imgSize, selectedPositions, images, selectedImages, this);

        recyclerView.setAdapter(trashFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        restoreBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Restore file")
                    .setMessage("Restore " + selectedImages.size() + " files")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Xử lý khi nhấn nút OK

                        new Thread(() -> {
                            for (String image : selectedImages) {
                                File temp = new File(image);
                                TrashManager.restoreFromTrash(this, temp.getName(), image);
                                images.remove(image);
                            }
                            selectedImages.clear();
                        }).start();
                        trashFrameAdapter.selectionModeEnabled = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    trashFrameAdapter.initFrameModels(images);
                                });
                            }
                        }, 1000);


                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Xử lý khi nhấn nút Cancel

                    })
                    .show();
        });
        deleteBtn.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete file forever")
                    .setMessage("If you delete these " + selectedImages.size() + " files, you can not restore")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Xử lý khi nhấn nút OK

                        new Thread(() -> {
                            for (String image : selectedImages) {
                                TrashManager.deleteFromTrash(image);
                                images.remove(image);
                            }
                            selectedImages.clear();
                        }).start();
                        trashFrameAdapter.selectionModeEnabled = false;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(() -> {
                                    trashFrameAdapter.initFrameModels(images);
                                });
                            }
                        }, 1000);


                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Xử lý khi nhấn nút Cancel

                    })
                    .show();
        });

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    // Override phương thức onItemClick() và onItemLongClick()
    @Override
    public void onItemClick(int position) {
        // Xử lý sự kiện click item
        if (selectedImages.isEmpty()) {
            restoreBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemLongClick(int position) {
        // Xử lý sự kiện long click item
        if (!selectedImages.isEmpty()) {
            restoreBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        }
    }
}
