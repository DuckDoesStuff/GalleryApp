package com.example.gallery.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class EditActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        String imgPath = getIntent().getStringExtra("imagePath");
        imageView = findViewById(R.id.imageView);
        Glide.with(this)
                .load(imgPath)
                .into(imageView);

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v->finish());

        ImageButton cropBtn = findViewById(R.id.crop_btn);
        cropBtn.setOnClickListener(v -> startCrop(Uri.fromFile(new File(imgPath))));



        ImageButton brightnessBtn = findViewById(R.id.brightness_btn);
        brightnessBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditActivity.this, ChangeBrightnessActivity.class);
            intent.putExtra("imagePath", imgPath); // Truyền đường dẫn ảnh tới activity mới nếu cần
            startActivity(intent);
        });

    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop uCrop = UCrop.of(sourceUri, destinationUri);

        // Thiết lập các tùy chọn cho việc cắt ảnh
        UCrop.Options options = new UCrop.Options();

        options.setToolbarColor(ContextCompat.getColor(this, R.color.background_dark)); // Màu của thanh công cụ
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light)); // Màu của thanh trạng thái
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.background_light)); // Màu của các widget trong thanh công cụ

        uCrop.withOptions(options);

        // Bắt đầu quá trình cắt ảnh
        uCrop.start(this);

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