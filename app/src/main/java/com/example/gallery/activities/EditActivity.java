package com.example.gallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class EditActivity extends AppCompatActivity {
    private  String imgPath;

    private ActivityResultLauncher<Intent> uCropLauncher;

    private String EditedPath;
    private ActivityResultLauncher<Intent> launcher;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imgPath = getIntent().getStringExtra("imagePath");

        imageView = findViewById(R.id.imageView);


        Glide.with(this)
                .load(imgPath)
                .into(imageView);

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v->finish());

        ImageButton cropBtn = findViewById(R.id.crop_btn);
        cropBtn.setOnClickListener(v -> {
            Uri sourceUri = Uri.fromFile(new File(imgPath));
            startCrop(sourceUri);
        });


        ImageButton brightnessBtn = findViewById(R.id.brightness_btn);
        brightnessBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditActivity.this, ChangeBrightnessActivity.class);
            intent.putExtra("imagePath", imgPath);
            launcher.launch(intent); // Sử dụng launcher đã được khai báo
        });
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    // Nhận đường dẫn của ảnh đã chỉnh sửa từ Intent
                    imgPath = data.getStringExtra("editedImagePath");
                    // Hiển thị ảnh đã chỉnh sửa

                    Glide.with(this)
                            .load(imgPath)
                            .into(imageView);
                }
            }
        });

        uCropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Do nothing here, just display the cropped image
                Intent data = result.getData();

                if (data != null) {
                    // Lấy Uri của ảnh đã cắt từ UCrop
                    Uri croppedUri = UCrop.getOutput(data);
                    if (croppedUri != null) {
                        // Hiển thị ảnh đã cắt lên ImageView
                        Glide.with(this)
                                .load(croppedUri)
                                .into(imageView);
                    }
                }
            }
        });

        ImageButton saveBtn = findViewById(R.id.save_btn);


    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop uCrop = UCrop.of(sourceUri, destinationUri);

        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.background_dark));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.background_light));

        uCrop.withOptions(options);

        // Launch UCrop using ActivityResultLauncher
        uCropLauncher.launch(uCrop.getIntent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            final Uri resultUri = UCrop.getOutput(data);

            if (resultUri != null) {



                Glide.with(this)
                        .load(resultUri)
                        .into(imageView);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                // Xử lý lỗi nếu có
            }
        }
    }
}