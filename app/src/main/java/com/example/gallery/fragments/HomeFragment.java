package com.example.gallery.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.FaceActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.activities.album.AlbumActivity;
import com.example.gallery.activities.pictures.ImageActivity;
import com.example.gallery.activities.pictures.ImageFrameAdapter;
import com.example.gallery.activities.pictures.MediaViewModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener, DatabaseObserver {
    File[] images;

    private MediaViewModel mediaViewModel;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private ImageFrameAdapter imageFrameAdapter;
    HashMap<String, List<FirebaseVisionPoint>> faceFeaturesMap = new HashMap<>();
    FirebaseVisionFaceDetector detector;

    private ArrayList<MediaModel> allMediaModels;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File faceDirectory = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".face");
        if (faceDirectory.exists()) {
            images = faceDirectory.listFiles();
            if (images != null && images.length > 0) {
                GalleryDB db = new GalleryDB(this.getContext());
                db.clearFaceTable();
                    for (File image:images) {
                        db.addToFaceTable(image.getAbsolutePath());
                    }
                db.close();
            }
        }
        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        try (GalleryDB db = new GalleryDB(this.getContext())) {
            ArrayList<MediaModel> faces = db.getAllFace();
            mediaViewModel.getMedia().setValue(faces);
        } catch (Exception e) {
            mediaViewModel.getMedia().setValue(new ArrayList<>());
        }

        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);
        try(GalleryDB db = new GalleryDB(getContext())) {
            allMediaModels = db.getAllLocalMedia();

            Log.d("PicturesFragment", "Pictures fragment got updated");
        } catch (Exception e) {
            Log.d("PicturesFragment", "Error getting media from database");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mainActivity = ((MainActivity) requireActivity());
        recyclerView = view.findViewById(R.id.photo_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onItemClick(int position) {
        Log.d("comat", "hihi");
        MediaModel mediaModel = mediaViewModel.getMedia(position);
        ArrayList<MediaModel> mediaRes = new ArrayList<>();
        File file = new File(mediaModel.localPath);
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        detector.detectInImage(image)
                .addOnSuccessListener(faces -> {
                    for (FirebaseVisionFace face : faces) {
                        // Duyệt qua tất cả các hình ảnh trong allMediaModels để tìm hình chứa người
                        for (MediaModel mediaModeltemp : allMediaModels) {
                            File fileTemp = new File(mediaModel.localPath);
                            Bitmap bitmapTemp = BitmapFactory.decodeFile(fileTemp.getAbsolutePath());
                            FirebaseVisionImage imageTemp = FirebaseVisionImage.fromBitmap(bitmapTemp);

                            detector.detectInImage(imageTemp)
                                    .addOnSuccessListener(facesTemp -> {
                                        for (FirebaseVisionFace faceTemp : facesTemp) {
                                            // So sánh các đặc trưng của khuôn mặt trong hình ảnh hiện tại với các đặc trưng của khuôn mặt gốc
                                            float similarityScore = calculateSimilarity(face, faceTemp);

                                            // Nếu có điểm tương đồng đủ lớn, bạn có thể giả định rằng hình này chứa người
                                            if (similarityScore > 0.5) {
                                                // Đây là hình ảnh chứa người, bạn có thể thực hiện các hành động cần thiết ở đây
                                                mediaRes.add(mediaModeltemp);
                                                Log.d("cohinh", "hihi");
                                            }
                                        }

                                    })
                                    .addOnFailureListener(e -> {
                                        // Xử lý khi phát hiện khuôn mặt thất bại
                                    });
                        }
                    }


                })
                .addOnFailureListener(e -> {
                    // Xử lý khi phát hiện khuôn mặt thất bại
                });
// Phương thức để tính điểm tương đồng giữa hai danh sách đặc trưng khuôn mặt

    }

    private float calculateSimilarity(FirebaseVisionFace face1, FirebaseVisionFace face2) {
        // Lấy các đường viền khuôn mặt từ hai khuôn mặt
        List<FirebaseVisionPoint> contour1 = face1.getContour(FirebaseVisionFaceContour.FACE).getPoints();
        List<FirebaseVisionPoint> contour2 = face2.getContour(FirebaseVisionFaceContour.FACE).getPoints();

        // Kiểm tra xem hai danh sách điểm có cùng kích thước không
        if (contour1.size() != contour2.size()) {
            return 0;
        }

        // Tính tổng khoảng cách Euclidean giữa các điểm trên các đường viền
        float sumSquaredDifferences = 0;
        for (int i = 0; i < contour1.size(); i++) {
            FirebaseVisionPoint point1 = contour1.get(i);
            FirebaseVisionPoint point2 = contour2.get(i);

            // Tính toán khoảng cách Euclidean giữa hai điểm
            float squaredDifference = (point1.getX() - point2.getX()) * (point1.getX() - point2.getX()) +
                    (point1.getY() - point2.getY()) * (point1.getY() - point2.getY());

            // Thêm vào tổng bình phương khoảng cách
            sumSquaredDifferences += squaredDifference;
        }

        // Tính khoảng cách trung bình bằng cách lấy căn bậc hai của tổng bình phương khoảng cách và chia cho số điểm
        float averageDistance = (float) Math.sqrt(sumSquaredDifferences / contour1.size());

        // Chuyển đổi khoảng cách trung bình thành điểm tương đồng bằng cách lấy nghịch đảo của nó
        // Điểm tương đồng càng cao nếu khoảng cách trung bình càng thấp và ngược lại
        float similarityScore = 1 / (1 + averageDistance);

        return similarityScore;
    }



    @Override
    public void onDatabaseChanged() {

    }
}