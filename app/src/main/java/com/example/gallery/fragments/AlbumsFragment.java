package com.example.gallery.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.AlbumFrameAdapter;
import com.example.gallery.component.ImageFrameAdapter;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment {
    boolean viewMode;

    private ArrayList<AlbumFrameAdapter.AlbumModel> albums;

    private ArrayList<AlbumFrameAdapter.AlbumModel> selectedAlbums;

    MainActivity mainActivity;
    public AlbumsFragment() {
        // Required empty public constructor
    }
    private String getThumbnailForAlbum(String albumName) {
        String[] projection = new String[] {
                MediaStore.Images.Media.DATA
        };

        // Định nghĩa điều kiện để chỉ lấy hình ảnh trong album cụ thể
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[] { albumName };

        Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            String thumbnailPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            // Di chuyển con trỏ đến ảnh cuối cùng trong album
            cursor.moveToLast();
            thumbnailPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            cursor.close();
            return thumbnailPath;
        }

        // Trả về null nếu không có ảnh trong album
        return null;
    }
    public void loadAlbums() {
        ArrayList<AlbumFrameAdapter.AlbumModel> albums = new ArrayList<>();

// Choose which columns to query
        String[] projection = new String[] {
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                "COUNT(*) AS " + MediaStore.Images.Media.DATA // Sử dụng COUNT(*) để đếm số lượng ảnh trong mỗi album
        };

// Group the query by bucket display name (album name)
        String selection = "1) GROUP BY (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME;

// Perform the query
        Cursor cursor = requireContext().
                getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    int imageCount = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)); // Số lượng ảnh trong album
                    String thumbnailPath = getThumbnailForAlbum(albumName); // Lấy ảnh thumbnail cho album

                    // Tạo một AlbumModel và thêm vào danh sách albums
                    AlbumFrameAdapter.AlbumModel album = new AlbumFrameAdapter.AlbumModel(albumName, imageCount, thumbnailPath);
                    albums.add(album);
                }
            } finally {
                cursor.close();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedAlbums = new ArrayList<AlbumFrameAdapter.AlbumModel>();
        viewMode = true;
        loadAlbums();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        mainActivity = ((MainActivity) requireActivity());

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int albSize = screenWidth / spanCount;

        RecyclerView recyclerView = view.findViewById(R.id.album_grid);
        AlbumFrameAdapter imageFrameAdapter = new ImageFrameAdapter(getContext(), imgSize, images, selectedImages, this);

        return view;
    }
}