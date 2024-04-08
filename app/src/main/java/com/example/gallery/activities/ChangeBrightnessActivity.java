package com.example.gallery.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.squareup.picasso.Picasso;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;

public class ChangeBrightnessActivity extends AppCompatActivity {
    private ImageView imageView;
    private String imagePath;

    private SeekBar brightnessSeekBar;
    private SeekBar contrastSeekBar;

    private GPUImage gpuImage;
    private float brightnessValue = 0.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_brightness);


        imagePath = getIntent().getStringExtra("imagePath");
        imageView = findViewById(R.id.imageView);

        Picasso.get().load(new File(imagePath)).into(imageView);

        ImageButton backBtn = findViewById(R.id.back_button);
        backBtn.setOnClickListener(v -> finish());


        brightnessSeekBar = findViewById(R.id.brightness_seek_bar);
        contrastSeekBar = findViewById(R.id.contrast_seek_bar);


        ImageButton brightnessBtn = findViewById(R.id.brightness_btn);
        brightnessBtn.setOnClickListener(v->onBrightnessButtonClick());


        ImageButton contrastBtn = findViewById(R.id.contrast_btn);
        contrastBtn.setOnClickListener(v->onContrastButtonClick());


        gpuImage = new GPUImage(this);
        gpuImage.setImage(new File(imagePath));

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValue = (float) progress / 50 -1; // Chuyển đổi giá trị từ 0-100 thành 0-1
                applyBrightnessFilter(brightnessValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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

    private void applyBrightnessFilter(float brightnessValue) {
        GPUImageBrightnessFilter brightnessFilter = new GPUImageBrightnessFilter();
        brightnessFilter.setBrightness(brightnessValue);
        gpuImage.setFilter(brightnessFilter);
        Glide.with(this).load(gpuImage.getBitmapWithFilterApplied()).fitCenter().into(imageView);
    }
}