package com.example.gallery.activities;

import static com.yalantis.ucrop.util.FileUtils.copyFile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.activities.ChangeBrightnessActivity;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {
    private String imgPath;
    private ImageView imageView;

    private String tempImagePath;

    private ActivityResultLauncher<Intent> uCropLauncher;
    private ActivityResultLauncher<Intent> brightnessLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        imgPath = getIntent().getStringExtra("imagePath");
        imageView = findViewById(R.id.imageView);
        tempImagePath = imgPath;

        Picasso.get().load(new File(imgPath)).into(imageView);



        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());

        ImageButton cropBtn = findViewById(R.id.crop_btn);
        cropBtn.setOnClickListener(v -> startCrop(Uri.fromFile(new File(imgPath))));

        ImageButton brightnessBtn = findViewById(R.id.brightness_btn);
        brightnessBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EditActivity.this, ChangeBrightnessActivity.class);
            intent.putExtra("imagePath", imgPath);
            brightnessLauncher.launch(intent);
        });

        ImageButton saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(v -> {
            saveEditedImage();
            finish();
        });

        uCropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                handleUCropResult(result.getData());
            }

        });

        brightnessLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                handleBrightnessResult(result.getData());
            }
        });
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));
        UCrop uCrop = UCrop.of(sourceUri, destinationUri);
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.background_dark));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.background_light));
        options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.background_light));
        uCrop.withOptions(options);
        uCropLauncher.launch(uCrop.getIntent(this));
    }

    private void handleUCropResult(Intent data) {
        if (data != null) {
            Uri croppedUri = UCrop.getOutput(data);
            if (croppedUri != null) {
                imgPath = croppedUri.getPath();

                Picasso.get().load(new File(imgPath)).into(imageView);

            }
        }

}

    private void handleBrightnessResult(Intent data) {
        if (data != null) {
            imgPath = data.getStringExtra("editedImagePath");
            if (imgPath != null) {
                Picasso.get().load(new File(imgPath)).into(imageView);


            }
        }
    }
    private void saveEditedImage() {
        File editedImageFile = new File(imgPath);

        if (!editedImageFile.exists()) {
            // Xử lý trường hợp không tìm thấy tệp ảnh đã chỉnh sửa
            Toast.makeText(this, "Edited image not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy tên tệp ảnh gốc
        String originalFileName = new File(tempImagePath).getName();

        // Lấy thời gian hiện tại dưới dạng milliseconds
        long currentTimeMillis = System.currentTimeMillis();

        // Tạo đường dẫn mới cho ảnh đã chỉnh sửa
        String directoryPath = new File(tempImagePath).getParent();
        String newFileName = originalFileName + "_" + currentTimeMillis + ".jpg";
        String savedImagePath = directoryPath + File.separator + newFileName;

        File savedImageFile = new File(savedImagePath);

        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // Mở luồng đọc từ tệp ảnh đã chỉnh sửa
            inputStream = new FileInputStream(editedImageFile);

            // Mở luồng ghi vào tệp mới
            outputStream = new FileOutputStream(savedImageFile);

            // Sao chép nội dung từ luồng đầu vào sang luồng đầu ra
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Hiển thị thông báo hoặc thực hiện các hành động cần thiết khi ảnh đã được lưu
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý ngoại lệ khi có lỗi xảy ra trong quá trình sao chép tệp
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        } finally {
            // Đóng luồng
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
