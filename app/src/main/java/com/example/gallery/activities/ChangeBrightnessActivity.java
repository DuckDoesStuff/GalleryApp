package com.example.gallery.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class ChangeBrightnessActivity extends AppCompatActivity {

    private static final int DELAY_MILLIS = 300;
    private String imagePath;
    private Handler mHandler = new Handler();
    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;
    private GPUImage gpuImage;
    private GPUImageView gpuImageView;
    private String editedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_brightness);


        imagePath = getIntent().getStringExtra("imagePath");
        gpuImageView = findViewById(R.id.imageView); // Initialize GPUImageView
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

        gpuImageView.setImage(new File(imagePath));

        editedImagePath = imagePath;


        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());


        brightnessSeekBar = findViewById(R.id.brightness_seek_bar);
        contrastSeekBar = findViewById(R.id.contrast_seek_bar);


        ImageButton brightnessBtn = findViewById(R.id.brightness_btn);
        brightnessBtn.setOnClickListener(v -> onBrightnessButtonClick());


        ImageButton contrastBtn = findViewById(R.id.contrast_btn);
        contrastBtn.setOnClickListener(v -> onContrastButtonClick());

        ImageButton saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(v -> {
            sendResult(editedImagePath);

        });

        gpuImage = new GPUImage(this);
        gpuImage.setImage(new File(imagePath));

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brightnessValue = (float) progress / 50 - 1;
                float contrastValue = (float) contrastSeekBar.getProgress() / 25;
                applyFilterInBackground(brightnessValue, contrastValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        contrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float contrastValue = (float) progress / 25;
                float brightnessValue = (float) brightnessSeekBar.getProgress() / 50 - 1;
                applyFilterInBackground(brightnessValue, contrastValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void onBrightnessButtonClick() {

        brightnessSeekBar.setVisibility(View.VISIBLE);
        contrastSeekBar.setVisibility(View.INVISIBLE);
    }

    private void onContrastButtonClick() {

        brightnessSeekBar.setVisibility(View.INVISIBLE);
        contrastSeekBar.setVisibility(View.VISIBLE);
    }

    private void applyFilterInBackground(final float brightnessValue, final float contrastValue) {
        // Hủy bỏ bất kỳ Runnable trước đó đang đợi để chạy
        mHandler.removeCallbacksAndMessages(null);

        // Đặt một Runnable mới để áp dụng bộ lọc sau một khoảng thời gian trì hoãn
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GPUImageFilterGroup filterGroup = new GPUImageFilterGroup();

                GPUImageBrightnessFilter brightnessFilter = new GPUImageBrightnessFilter();
                brightnessFilter.setBrightness(brightnessValue);

                GPUImageContrastFilter contrastFilter = new GPUImageContrastFilter();
                contrastFilter.setContrast(contrastValue);

                filterGroup.addFilter(brightnessFilter);
                filterGroup.addFilter(contrastFilter);

                gpuImage.setFilter(filterGroup);
                final Bitmap filteredBitmap = gpuImage.getBitmapWithFilterApplied();

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(filteredBitmap, gpuImageView.getWidth(), gpuImageView.getHeight(), true);
                editedImagePath = saveBitmapAndGetPath(scaledBitmap);
                // Lưu lại đường dẫn của ảnh đã chỉnh sửa

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);

                        gpuImageView.setImage(scaledBitmap);
                    }
                });
            }
        }, DELAY_MILLIS);
    }

    private String saveBitmapAndGetPath(Bitmap bitmap) {
        File editedImageFile = new File(getExternalFilesDir(null), "edited_image.jpg");
        try {
            FileOutputStream outputStream = new FileOutputStream(editedImageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return editedImageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendResult(String editedImagePath) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("editedImagePath", editedImagePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


}