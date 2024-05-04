package com.example.gallery.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.component.dialog.BottomDialogDuplicateItem;
import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.Semaphore;

class ReplaceFileWithItself {
    static byte[] readFileToMemory(String filePath) throws IOException {
        // Read file data into memory
        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileData = new byte[(int) file.length()];
            fis.read(fileData);
            return fileData;
        }
    }

    static boolean deleteFile(String filePath) {
        // Delete the file
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    static void writeMemoryDataToFile(byte[] data, String filePath) throws IOException {
        // Write the data from memory to the same file path
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        }
    }
}

public class AlbumManager extends AppCompatActivity {

    private boolean applyToAll;
    private boolean chooseSkip;
    private boolean chooseReplace;
    private boolean chooseRename;

    private final Semaphore semaphore = new Semaphore(1);

    private BottomDialogDuplicateItem.OnDuplicateItemListener duplicateItemListener = new BottomDialogDuplicateItem.OnDuplicateItemListener() {
        @Override
        public void onApplyToAll(boolean applyAll) {
            applyToAll = applyAll;
        }

        @Override
        public void onSkip(boolean skip) {
            chooseSkip = skip;
        }

        @Override
        public void onReplace(boolean replace) {
            chooseReplace = replace;
        }

        @Override
        public void onRename(boolean rename) {
            chooseRename = rename;
        }

        @Override
        public void onDismiss() {
            Log.d("AlbumManager", "Dialog dismissed");
            semaphore.release();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            // Handle intent
            String action = intent.getStringExtra("action");
            if(action == null) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            switch (action) {
                case "add": {
                    ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("mediaModels");
                    AlbumModel albumModel = intent.getParcelableExtra("albumModel");
                    new Thread(() -> {
                        if (mediaModels != null) {
                            moveMedia(this, mediaModels, albumModel);
                        }
                    }).start();
                    break;
                }
                case "create": {
                    String albumName = intent.getStringExtra("albumName");
                    AlbumModel albumModel = createNewAlbum(this, albumName);
                    if (albumModel != null) {
                        setResult(RESULT_OK);
                        Log.d("AlbumManager", "Album created successfully");
                    } else {
                        setResult(RESULT_CANCELED);
                        Log.d("AlbumManager", "Album failed to create");
                    }
                    finish();
                    break;
                }
            }

        }
        else {
            finish();
        }
    }

    private void moveMedia(Context context, ArrayList<MediaModel> mediaModels, AlbumModel albumModel) {
        boolean success = true;
        applyToAll = false;
        chooseSkip = false;
        chooseReplace = false;
        chooseRename = false;
        for (MediaModel mediaModel : mediaModels) {
            if (!moveMedia(context, mediaModel, albumModel)) {
                success = false;
            }
        }
        notifyMediaStoreScan(albumModel.localPath);
        setResult(success ? RESULT_OK : RESULT_CANCELED);
        finish();
    }

    private boolean moveMedia(Context context, MediaModel mediaModel, AlbumModel albumModel) {
        try {
            Log.d("AlbumManager", "File: " + mediaModel.localPath);
            Log.d("AlbumManager", "Album: " + albumModel.localPath);
            if(mediaModel.localPath.isEmpty()) {
                return false; // MediaModel does not have a local path
            }
            String sourceFilePath = mediaModel.localPath;
            File sourceFile = new File(sourceFilePath);

            if (albumModel.localPath.isEmpty()) {
                return false; // AlbumModel does not have a local path
            }
            String destinationPath = albumModel.localPath + "/" + sourceFile.getName();

            File destinationFileCheck = new File(destinationPath);
            // Check if there is a file with the same path
            semaphore.acquire();
            boolean fileExists = destinationFileCheck.exists();
            if (fileExists && !applyToAll) {
                BottomDialogDuplicateItem bottomDialogDuplicateItem = new BottomDialogDuplicateItem();
                bottomDialogDuplicateItem.setOnDuplicateItemListener(duplicateItemListener);
                bottomDialogDuplicateItem.show(getSupportFragmentManager(), "DuplicateItem");
            }else {
                semaphore.release();
            }
            semaphore.acquire();
            if(fileExists) {
                if (chooseSkip) {
                    semaphore.release();
                    return true; // Skip this file
                } else if (chooseReplace) {
                    // Store the current source file data
                    byte[] fileData = ReplaceFileWithItself.readFileToMemory(sourceFilePath);
                    // Delete the source file
                    if (!ReplaceFileWithItself.deleteFile(sourceFilePath)) {
                        return false; // Error occurred while deleting file
                    }
                    // Write the stored data to the destination file
                    ReplaceFileWithItself.writeMemoryDataToFile(fileData, destinationPath);

                    mediaModel.localPath = destinationPath;
                    try (GalleryDB db = new GalleryDB(context)) {
                        if(Objects.equals(albumModel.albumThumbnail, "")) {
                            albumModel.albumThumbnail = mediaModel.localPath;
                            db.updateAlbum(albumModel);
                        }
                        db.updateMedia(mediaModel);
                    }
                    semaphore.release();
                    return true;

                } else if (chooseRename) {
                    Log.d("AlbumManager", "Renaming file");
                    String fileName = sourceFile.getName();
                    String[] split = fileName.split("\\.");
                    String name = split[0];
                    String extension = split[1];
                    int i = 1;
                    while (destinationFileCheck.exists()) {
                        destinationFileCheck = new File(albumModel.localPath + "/" + name + "(" + i + ")." + extension);
                        i++;
                    }
                }
            }

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(destinationFileCheck);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            mediaModel.localPath = destinationFileCheck.getAbsolutePath();
            Log.d("AlbumManager", "File: " + mediaModel.localPath);
            Log.d("AlbumManager", "Album: " + albumModel.localPath);

            if (sourceFile.delete()) {
                try (GalleryDB db = new GalleryDB(context)) {
                    if(Objects.equals(albumModel.albumThumbnail, "")) {
                        albumModel.albumThumbnail = mediaModel.localPath;
                        db.updateAlbum(albumModel);
                    }
                    db.updateMedia(mediaModel);
                }
                semaphore.release();
                return true;
            } else {
                semaphore.release();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error moving file");
            return false; // Error occurred while moving file
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error acquiring semaphore");
            return false;
        }
    }

    private AlbumModel createNewAlbum(@NonNull Context context, String albumName) {
        File album = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);

        if (!album.exists()) {
            if (album.mkdirs()) {
                // Return directory path
                String albumPath = album.getAbsolutePath();
                AlbumModel albumModel = new AlbumModel();
                albumModel.setAlbumName(albumName)
                        .setLocalPath(albumPath)
                        .setCreatedAt(System.currentTimeMillis());

                try(GalleryDB db = new GalleryDB(context)) {
                    db.addToAlbumTable(new ArrayList<>(Collections.singletonList(albumModel)));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("AlbumManager", "Error adding to database");
                    return null; // Error occurred while adding to database
                }

                return albumModel;
            }
        }else {
            GalleryDB galleryDB = new GalleryDB(context);
            SQLiteDatabase db = galleryDB.getWritableDatabase();
            // Check if already in database
            Cursor cursor = db.rawQuery("SELECT * FROM albums WHERE album_name = ?", new String[]{albumName});
            if(cursor.getCount() > 0) {
                cursor.close();
                return null; // Album already exists
            }
            cursor.close();

            AlbumModel albumModel = new AlbumModel();
            albumModel.setAlbumName(albumName)
                    .setLocalPath(album.getAbsolutePath())
                    .setCreatedAt(System.currentTimeMillis());

            galleryDB.addToAlbumTable(new ArrayList<>(Collections.singletonList(albumModel)));
            return albumModel;
        }
        return null; // Error occurred while creating album
    }

    private void notifyMediaStoreScan(String filePath) {
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{filePath}, null, (path, uri) -> {
            // MediaScannerConnection callback
        });
    }
}
