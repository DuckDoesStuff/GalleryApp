package com.example.gallery.activities.pictures;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.example.gallery.R;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

;

public class ImageActivity extends AppCompatActivity {
    ArrayList<MediaModel> mediaModels;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Intent intent = getIntent();
        final int position;
        if (intent != null) {
            mediaModels = intent.getParcelableArrayListExtra("mediaModels");
            position = intent.getIntExtra("initial", 0);
        } else {
            mediaModels = new ArrayList<>();
            position = -1;
        }

        ImageButton imageButton = findViewById(R.id.back_button);
        imageButton.setOnClickListener(v -> finish());

        ImageButton editButton = findViewById(R.id.edit_btn);
        ImageButton deleteButton = findViewById(R.id.trash_btn);
        ImageButton shareButton = findViewById(R.id.share_button);
        ImageButton heartButton = findViewById(R.id.heart_button);


        heartButton.setOnClickListener(v -> {
            if (mediaModels.get(viewPager2.getCurrentItem()).favorite == true) {
                heartButton.setImageResource(R.drawable.favorite);
                mediaModels.get(viewPager2.getCurrentItem()).setFavorite(false);
                try (GalleryDB db = new GalleryDB(this)) {
                    db.updateMedia(mediaModels.get(viewPager2.getCurrentItem()));
                }
                Log.d("noti", "true");
            } else {
                heartButton.setImageResource(R.drawable.favorite_filled);
                mediaModels.get(viewPager2.getCurrentItem()).setFavorite(true);
                try (GalleryDB db = new GalleryDB(this)) {
                    db.updateMedia(mediaModels.get(viewPager2.getCurrentItem()));
                }
                Log.d("noti", "false");

            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lấy đường dẫn của hình ảnh đang hiển thị trong ViewPager2
                String currentImagePath = mediaModels.get(viewPager2.getCurrentItem()).localPath;

                // Tạo một Uri từ đường dẫn của hình ảnh
                Uri imageUri = FileProvider.getUriForFile(com.example.gallery.activities.pictures.ImageActivity.this,
                        "com.example.gallery", new File(currentImagePath));

                // Tạo Intent để chia sẻ hình ảnh
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

                // Cho phép các ứng dụng khác đọc dữ liệu từ FileProvider của bạn
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Mở Activity chia sẻ và chọn ứng dụng để chia sẻ hình ảnh
                startActivity(Intent.createChooser(shareIntent, "Share Image"));
            }
        });
        deleteButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Delete this file")
                    .setMessage("Are you sure to delete this file?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        int currentPosition = viewPager2.getCurrentItem();
                        mediaModels.remove(currentPosition);
                        viewPagerAdapter.notifyDataSetChanged();

                        if (mediaModels.isEmpty()) {
                            finish();
                        } else {
                            if (currentPosition < mediaModels.size()) {
                                viewPager2.setCurrentItem(currentPosition, false);
                            } else {
                                viewPager2.setCurrentItem(currentPosition - 1, false);
                            }
                        }

//                    String currentImagePath = mediaModels.get(currentPosition).localPath;
//                    TrashManager.moveToTrash(ImageActivity.this, currentImagePath);

                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Xử lý khi nhấn nút Cancel

                    })
                    .show();
        });
        editButton.setOnClickListener(v -> {
            String currentImagePath = mediaModels.get(viewPager2.getCurrentItem()).localPath;
            File currentImageFile = new File(currentImagePath);
            File currentImageDirectory = currentImageFile.getParentFile();
            Intent editIntent = new Intent(ImageActivity.this, DsPhotoEditorActivity.class);
            editIntent.setData(Uri.fromFile(new File(currentImagePath)));
            editIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, currentImageDirectory.getAbsolutePath());
            editIntent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
            // Set background color
            editIntent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR, Color.parseColor("#FF000000"));
            startActivity(editIntent); // Khởi động hoạt động


        });

        viewPager2 = findViewById(R.id.view_pager);
        viewPagerAdapter = new ViewPagerAdapter(mediaModels, this);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setCurrentItem(position, false);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Kiểm tra và cập nhật trạng thái của trái tim khi trang hiện tại thay đổi
                if (mediaModels.size() > position) {
                    MediaModel currentMedia = mediaModels.get(position);

                    if (currentMedia.favorite) {
                        heartButton.setImageResource(R.drawable.favorite_filled);
                    } else {
                        heartButton.setImageResource(R.drawable.favorite);
                    }
                }
            }
        });


        if (mediaModels.get(viewPager2.getCurrentItem()).favorite) {
            heartButton.setImageResource(R.drawable.favorite_filled);
            Log.d("favorite", "true");
        } else {
            heartButton.setImageResource(R.drawable.favorite);
            Log.d("favorite", "false");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Debug", "Image activity gone");
    }

}