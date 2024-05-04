package com.example.gallery.activities.pictures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;
import com.example.gallery.activities.FavoriteActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.activities.TrashActivity;
import com.example.gallery.activities.search.SearchImageActivity;
import com.example.gallery.component.dialog.AlbumPickerActivity;
import com.example.gallery.utils.AlbumManager;
import com.example.gallery.utils.TrashManager;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.DatabaseObserver;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;


import java.util.HashSet;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class PicutresFragment extends Fragment implements ImageFrameAdapter.ImageFrameListener, DatabaseObserver {
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    ArrayList<MediaModel> mediaModels;
    LinearLayout bottomSheet;
    boolean viewMode = true;
    MainActivity mainActivity;
    ImageFrameAdapter imageFrameAdapter;
    RecyclerView recyclerView;
    private ArrayList<MediaModel> selectedImages;
    ActivityResultLauncher<Intent> albumManagerLauncher;
    ActivityResultLauncher<Intent> albumPickerLauncher;
    ActivityResultLauncher<Intent> trashManagerLauncher;

    private FirebaseVisionFaceDetector detector;
    private MediaViewModel mediaViewModel;

    String facePath;

    public PicutresFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDatabaseChanged() {
        getFromDatabase();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();
        if (selectedImages == null)
            selectedImages = new ArrayList<>();
        GalleryDB.addMediaObserver(this);

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        // Updates UI in here
        Observer<ArrayList<MediaModel>> mediaObserver = mediaModels -> {
            // Updates UI in here
            Log.d("PicturesFragment", "Media observer called with " + mediaModels.size() + " items");
        };
        mediaViewModel.getMedia().observe(this, mediaObserver);

        mediaViewModel.getSelectedMedia().setValue(new ArrayList<>());
        // Updates UI in here
        Observer<ArrayList<Integer>> selectedMediaObserver = selectedMedia -> {
            // Updates UI in here
            if (!selectedMedia.isEmpty()) onShowBottomSheet();
            else onHideBottomSheet();
            Log.d("PicturesFragment", "Selected media observer called with " + selectedMedia.size() + " items");
        };
        mediaViewModel.getSelectedMedia().observe(this, selectedMediaObserver);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        Log.d("PicturesFragment", "Initialized");
    }
    private void detectFacesInMediaModels(ArrayList<MediaModel> mediaModels) {
        initProcessedFace();
        for (MediaModel mediaModel : mediaModels) {
            try {
                Log.d("face", "co face1");

                File file = new File(mediaModel.localPath);
                Log.d("path",file.getAbsolutePath());

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                detector.detectInImage(image)
                        .addOnSuccessListener(faces -> {
                            // Xử lý các khuôn mặt được phát hiện ở đây
                            processDetectedFaces(file, faces);
                            Log.d("facesed", "co face1");

                        })
                        .addOnFailureListener(e -> {
                            // Xử lý lỗi khi detect khuôn mặt
                            e.printStackTrace();
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private HashSet<FirebaseVisionFace> processedFaces = new HashSet<>();
    private  void initProcessedFace() {
        File faceDirectory = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".face");
        if (faceDirectory.exists()) {
            File[] imageFiles = faceDirectory.listFiles();
            if (imageFiles != null && imageFiles.length > 0) {
                // Duyệt qua mỗi tệp trong danh sách và xử lý nó
                for (File imageFile : imageFiles) {
                    // Làm cái gì đó với mỗi tệp ảnh, ví dụ: hiển thị tên tệp, đường dẫn, v.v.
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
                    detector.detectInImage(image)
                            .addOnSuccessListener(faceProcesseds -> {
                                // Xử lý các khuôn mặt được phát hiện
                                for (FirebaseVisionFace face : faceProcesseds) {
                                    // Thêm khuôn mặt vào processedFaces
                                    if (!processedFaces.contains(face)) {
                                        processedFaces.add(face);
                                    }
                                }
                            });
                }
            }
        }
    }
    private void processDetectedFaces(File file, List<FirebaseVisionFace> faces) {
        // Xử lý các khuôn mặt được phát hiện trong mediaModel ở đây
        // Ví dụ: lưu các khuôn mặt vào thư mục ẩn .face
        Log.d("facdo", "co face");

        for (FirebaseVisionFace face : faces) {
            if (!processedFaces.contains(face)) {
                processedFaces.add(face);
                Log.d("face", "co face");
                saveFaceImage(face, file);
            }

        }
    }

    private void saveFaceImage(FirebaseVisionFace face, File mediaModel) {
        Bitmap originalBitmap = BitmapFactory.decodeFile(mediaModel.getAbsolutePath());
        // Đảm bảo rằng bạn có quyền ghi vào thư mục này
        File faceDir = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".face");
        if (!faceDir.exists()) {
            faceDir.mkdirs();
            facePath = faceDir.getPath();
        }

        int faceWidth = face.getBoundingBox().width();
        int faceHeight = face.getBoundingBox().height();
        int faceX = face.getBoundingBox().left;
        int faceY = face.getBoundingBox().top;

        // Cắt ảnh của khuôn mặt từ ảnh gốc
        Bitmap faceBitmap = Bitmap.createBitmap(originalBitmap, faceX, faceY, faceWidth, faceHeight);

        // Lưu ảnh khuôn mặt vào thư mục ẩn
        String faceFileName = "face_" + System.currentTimeMillis() + ".jpg";
        File faceFile = new File(faceDir, faceFileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(faceFile);
            faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d("PicturesFragment", "Saved face image: " + faceFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PicturesFragment", "Failed to save face image");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GalleryDB.removeMediaObserver(this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picutres, container, false);
        mainActivity = ((MainActivity) requireActivity());
        recyclerView = view.findViewById(R.id.photo_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(10);


        ImageButton dropdownButton = view.findViewById(R.id.settings);
        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if (item.getItemId() == R.id.trash) {
                    Intent intent = new Intent(getContext(), TrashActivity.class);
                    mainActivity.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.favorite) {
                    Intent intent = new Intent(getContext(), FavoriteActivity.class);
                    mainActivity.startActivity(intent);
                } else if (item.getItemId() == R.id.by_latest) {
                    mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
                    mediaViewModel.getMedia().setValue(mediaModels);
                    return true;
                } else if (item.getItemId() == R.id.by_oldest) {
                    mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
                    Collections.reverse(mediaModels);
                    mediaViewModel.getMedia().setValue(mediaModels);
                    return true;
                }
                else if (item.getItemId() == R.id.by_album_name) {
                    Comparator<MediaModel> albumNameComparator = new Comparator<MediaModel>() {
                        @Override
                        public int compare(MediaModel o1, MediaModel o2) {
                            return o1.albumName.compareTo(o2.albumName);  // Sắp xếp theo thứ tự bảng chữ cái
                        }
                    };

// Sắp xếp danh sách theo tên album
                    Collections.sort(mediaModels, albumNameComparator);

// Sau khi sắp xếp, bạn có thể cập nhật ViewModel
                    mediaViewModel.getMedia().setValue(mediaModels);


                    return true;
                }

                return true;

            });

            popupMenu.show();
        });

        ImageButton searchButton = view.findViewById(R.id.search);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchImageActivity.class);
            mainActivity.startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheet = requireView().findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        setUpBottomSheet();

        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        int spanCount = 3; // Change this to change the number of columns
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int imgSize = screenWidth / spanCount;

        imageFrameAdapter = new ImageFrameAdapter(imgSize, this, this);
        recyclerView.setAdapter(imageFrameAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));

        albumManagerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            selectedImages.clear();
                            mediaViewModel.clearSelectedMedia();
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Toast.makeText(getContext(), "Media added to album", Toast.LENGTH_SHORT).show();
                            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to add media to album", Toast.LENGTH_SHORT).show();
                            }
                        });

        albumPickerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent resultData = result.getData();
                                if (resultData != null) {
                                    AlbumModel pickedAlbum = resultData.getParcelableExtra("album");
                                    if (pickedAlbum != null) {
                                        Log.d("PicturesFragment", "Starting AlbumManager");
                                        Intent newIntent = new Intent(mainActivity, AlbumManager.class);
                                        newIntent.putExtra("mediaModels", selectedImages);
                                        newIntent.putExtra("albumModel", pickedAlbum);
                                        newIntent.putExtra("action", "add");
                                        albumManagerLauncher.launch(newIntent);
                                    }
                                }
                            }else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                selectedImages.clear();
                                mediaViewModel.clearSelectedMedia();
                            }
                        });

        trashManagerLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            selectedImages.clear();
                            mediaViewModel.clearSelectedMedia();
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Toast.makeText(getContext(), "Media moved to trash", Toast.LENGTH_SHORT).show();
                            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                                Toast.makeText(getContext(), "Failed to move media to trash", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void getFromDatabase() {
        try(GalleryDB db = new GalleryDB(getContext())) {
            mediaModels = db.getAllLocalMedia();
            mediaModels.sort((o1, o2) -> Long.compare(o2.dateTaken, o1.dateTaken));
            mediaViewModel.getMedia().setValue(mediaModels);
            detectFacesInMediaModels(mediaModels);
            Log.d("PicturesFragment", "Pictures fragment got updated");
        } catch (Exception e) {
            Log.d("PicturesFragment", "Error getting media from database");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getFromDatabase();
    }

    private void setUpBottomSheet() {
        TransitionSet transitionSet = new TransitionSet()
                .addTransition(new Slide())
                .setDuration(400);

        TransitionManager.beginDelayedTransition(bottomSheet, transitionSet);

        Button trashBtn = bottomSheet.findViewById(R.id.trashBtn);
        trashBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, TrashManager.class);
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                selectedImages.clear();
                for (int position : selectedPositions) {
                    selectedImages.add(mediaViewModel.getMedia(position));
                }
            }
            intent.putParcelableArrayListExtra("mediaModels", selectedImages);
            intent.putExtra("action", "trash");
            trashManagerLauncher.launch(intent);
        });

        Button addBtn = bottomSheet.findViewById(R.id.addToBtn);
        addBtn.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, AlbumPickerActivity.class);
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                selectedImages.clear();
                for (int position : selectedPositions) {
                    selectedImages.add(mediaViewModel.getMedia(position));
                }
            }
            intent.putParcelableArrayListExtra("mediaModels", selectedImages);
            albumPickerLauncher.launch(intent);
        });
        Button shareBtn = bottomSheet.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(v -> {
            ArrayList<Integer> selectedPositions = mediaViewModel.getSelectedMedia().getValue();
            if (selectedPositions != null) {
                ArrayList<Uri> imageUris = new ArrayList<>();
                selectedImages.clear();
                Log.d("hii","share");
                for (int position : selectedPositions) {
                    // Use position to retrieve MediaModel from selectedImages list
                    selectedImages.add(mediaViewModel.getMedia(position));
                    MediaModel mediaModel = mediaViewModel.getMedia(position);
                    String imagePath = mediaModel.localPath;
                    File imageFile = new File(imagePath);
                    Uri imageUri = FileProvider.getUriForFile(requireContext(),
                            "com.example.gallery", imageFile);
                    imageUris.add(imageUri);
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("image/*");
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Share Images"));
                mediaViewModel.clearSelectedMedia();
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        if (viewMode) {
            Intent intent = new Intent(mainActivity, ImageActivity.class);
            ArrayList<MediaModel> mediaModels = mediaViewModel.getMedia().getValue();
            for (MediaModel media: mediaModels) {
                if (media.favorite) {
                    Log.d("favorite", "true");
                } else {
                    Log.d("favorite", "false");

                }
            }
            intent.putParcelableArrayListExtra("mediaModels", mediaModels);
            intent.putExtra("initial", position);
            mainActivity.startActivity(intent);
        }
    }

    private void onShowBottomSheet() {
        viewMode = false;
        mainActivity.setBottomNavigationViewVisibility(View.GONE);
        recyclerView.setPadding(0, 0, 0, bottomSheet.getHeight());
        requireView().post(() -> {
            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
    }

    private void onHideBottomSheet() {
        viewMode = true;
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        recyclerView.setPadding(0, 0, 0, 0);
        requireView().post(() -> {
            mainActivity.setBottomNavigationViewVisibility(View.VISIBLE);
        });
    }


}