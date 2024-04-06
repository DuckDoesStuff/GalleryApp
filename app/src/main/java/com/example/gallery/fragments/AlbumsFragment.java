package com.example.gallery.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.AlbumActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.AlbumFrameAdapter;
import com.example.gallery.component.dialog.BottomDialog;
import com.example.gallery.utils.GalleryDB;
import com.example.gallery.utils.MediaContentObserver;
import com.example.gallery.utils.MediaFetch;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment implements AlbumFrameAdapter.AlbumFrameListener, MediaContentObserver.OnMediaUpdateListener {
    boolean viewMode;
    ArrayList<MediaFetch.MediaModel> modelArrayList;
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
    public void onDestroy() {
        super.onDestroy();
        MediaFetch.getInstance(null).unregisterListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton newAlbumButton = view.findViewById(R.id.plus);
        newAlbumButton.setOnClickListener(v -> {
            // Show dialog to create new album
            new BottomDialog().show(getParentFragmentManager(), "bottom_dialog");
        });
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
        albumFrameAdapter = new AlbumFrameAdapter(this, albums);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(albumFrameAdapter);

        return view;
    }

    @Override
    public void onMediaUpdate(ArrayList<MediaFetch.MediaModel> modelArrayList) {
        albums.clear();
        List<String> bucketIds = MediaFetch.getBucketIds(this.requireContext());
        ArrayList<GalleryDB.AlbumScheme> albumSchemes = new ArrayList<>();
        this.modelArrayList= modelArrayList;
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
                albumSchemes.add(new GalleryDB.AlbumScheme(albumName, bucketId, false, null));
            }
        }

        // Update GalleryDB
        new Thread(() -> {
            try(GalleryDB db = new GalleryDB(this.requireContext())) {
                db.updateAlbums(albumSchemes);
            }catch (Exception e){
                e.printStackTrace();
                Log.d("DB", "Error updating albums");
            }
        }).start();

        // Add the other albums which MediaStore didnt index from GalleryDB
        try(GalleryDB db = new GalleryDB(this.requireContext())) {
            ArrayList<GalleryDB.AlbumScheme> dbAlbums = db.getAlbums();
            for (GalleryDB.AlbumScheme albumScheme : dbAlbums) {
                boolean found = false;
                for (AlbumFrameAdapter.AlbumModel album : albums) {
                    if (album.albumName.equals(albumScheme.albumName)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    albums.add(new AlbumFrameAdapter.AlbumModel(null, albumScheme.albumName, 0, null));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d("DB", "Error updating albums");
        }

        requireActivity().runOnUiThread(()->{
            albumFrameAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onItemClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemClick(position);
        ArrayList<MediaFetch.MediaModel> mediaList = MediaFetch.mediaFromBucketID(modelArrayList, albums.get(position).id);
        Intent intent = new Intent(getContext(), AlbumActivity.class);
        intent.putExtra("images", mediaList);
        intent.putExtra("name", albums.get(position).albumName);
        intent.putExtra("initial", position);
        mainActivity.startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        AlbumFrameAdapter.AlbumFrameListener.super.onItemLongClick(position);
    }
}