package com.example.gallery.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.AlbumFrameAdapter;
import com.example.gallery.component.ImageFrameAdapter;

import java.util.ArrayList;
import java.util.List;

import com.example.gallery.utils.MediaContentObserver;
import com.example.gallery.utils.MediaFetch;
import java.util.Collections;

public class AlbumsFragment extends Fragment implements MediaContentObserver.OnMediaUpdateListener {
    boolean viewMode;

    private ArrayList<AlbumFrameAdapter.AlbumModel> albums;

    private ArrayList<AlbumFrameAdapter.AlbumModel> selectedAlbums;

    AlbumFrameAdapter albumFrameAdapter;

    MainActivity mainActivity;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    private String getThumbnailForAlbum(String albumName) {
        String[] projection = new String[]{
                MediaStore.Images.Media.DATA
        };

        // Định nghĩa điều kiện để chỉ lấy hình ảnh trong album cụ thể
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = new String[]{albumName};

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
        albums = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        };

        Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Retrieve data from the cursor
                String albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
//                int imageCount = cursor.getInt(cursor.getColumnIndexOrThrow("image_count"));

//                albums.add(new AlbumFrameAdapter.AlbumModel(albumName, imageCount, ""));
                Log.d("Album", albumName);
            }
            cursor.close(); // Close the cursor when done
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedAlbums = new ArrayList<AlbumFrameAdapter.AlbumModel>();
        viewMode = true;
        albums = new ArrayList<>();
        MediaFetch.getInstance(null).registerListener(this);
        MediaFetch.getInstance(null).fetchMedia(false);

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
        albumFrameAdapter = new AlbumFrameAdapter(albums);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(albumFrameAdapter);

        return view;
    }

    @Override
    public void onMediaUpdate(ArrayList<MediaFetch.MediaModel> modelArrayList) {
        albums.clear();
        List<String> bucketIds = MediaFetch.getBucketIds(this.requireContext());
        for (String bucketId : bucketIds) {
            // Lấy danh sách media từ bucket ID (có thể là ảnh hoặc video)
            ArrayList<MediaFetch.MediaModel> mediaList = MediaFetch.mediaFromBucketID(modelArrayList, bucketId);

            // Kiểm tra xem album có media không
            if (!mediaList.isEmpty()) {
                // Lấy thông tin của album (tên album, số lượng media)
                String albumName = mediaList.get(0).bucketName; // Lấy tên album từ media đầu tiên trong danh sách
                int numOfMedia = mediaList.size();
                MediaFetch.sortArrayListModel(mediaList, MediaFetch.SORT_BY_DATE_TAKEN, MediaFetch.SORT_DESC);
                String thumbnail = mediaList.get(0).data; // Đây là nơi để lấy hình ảnh thumbnail, bạn có thể thay thế bằng logic tương ứng

                // Tạo đối tượng AlbumModel và thêm vào ArrayList
                albums.add(new AlbumFrameAdapter.AlbumModel(bucketId, albumName, numOfMedia, thumbnail));
            }
        }
        requireActivity().runOnUiThread(()->{
            albumFrameAdapter.notifyDataSetChanged();
        });
    }
}