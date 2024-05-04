package com.example.gallery.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TrashManager extends AppCompatActivity {

    private String trashPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createTrash();

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getStringExtra("action");
            if(action == null) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("mediaModels");
            if (Objects.requireNonNull(mediaModels).isEmpty()) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            switch (action) {
                case "trash":
                    setResult(moveToTrash(this, mediaModels) ? RESULT_OK : RESULT_CANCELED);
                    finish();
                    break;
                case "restore":
                    setResult(restoreFromTrash(this, mediaModels) ? RESULT_OK : RESULT_CANCELED);
                    finish();
                    break;
                case "delete":
                    setResult(deleteFromTrash(this, mediaModels) ? RESULT_OK : RESULT_CANCELED);
                    finish();
                    break;
            }
        }
    }
    private void notifyMediaStoreScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, (path, uri) -> {
            // MediaScannerConnection callback
        });
    }

    private void createTrash() {
        File trashAlbum = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".trash");
        if (!trashAlbum.exists())
            if(trashAlbum.mkdirs())
                trashPath = trashAlbum.getAbsolutePath();
            else {
                trashPath = null;
                Toast.makeText(this, "Error creating trash directory", Toast.LENGTH_SHORT).show();
            }
        else
            trashPath = trashAlbum.getAbsolutePath();
    }

    private boolean deleteFromTrash(Context context, ArrayList<MediaModel> mediaModels) {
        boolean success = true;
        for (MediaModel mediaModel : mediaModels) {
            if (!deleteFromTrash(context, mediaModel)) {
                success = false;
            }
        }
        notifyMediaStoreScan(context, trashPath);
        GalleryDB.notifyTrashObservers();
        return success;
    }

    private boolean deleteFromTrash(Context context, MediaModel mediaModel) {
        try {
            String mediaPath = mediaModel.trashPath;
            File sourceFile = new File(mediaPath);
            if (sourceFile.delete()) {
                try (GalleryDB db = new GalleryDB(context)) {
                    db.removeFromTrashTable(mediaModel);
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error deleting item from trash");
            return false;
        }
    }

    private boolean restoreFromTrash(Context context, ArrayList<MediaModel> mediaModels) {
        boolean success = true;
        for (MediaModel mediaModel : mediaModels) {
            if (!restoreFromTrash(context, mediaModel)) {
                success = false;
            }
        }
        notifyMediaStoreScan(context, Environment.DIRECTORY_DCIM);
        GalleryDB.notifyMediaObservers();
        GalleryDB.notifyAlbumObservers();
        GalleryDB.notifyTrashObservers();
        return success;
    }

    private boolean restoreFromTrash(Context context, MediaModel mediaModel) {
        try {
            String trashPath = mediaModel.trashPath;

            File sourceFile = new File(trashPath);
            String destinationPath = mediaModel.localPath;
            File destinationFile = new File(destinationPath);

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            if (sourceFile.delete()) {
                try (GalleryDB db = new GalleryDB(context)) {
                    db.removeFromTrashTable(mediaModel);
                    db.addToMediaTable(new ArrayList<>(Collections.singletonList(mediaModel)));
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error restoring item");
            return false;
        }
    }

    private boolean moveToTrash(Context context, ArrayList<MediaModel> mediaModels) {
        boolean success = true;
        for (MediaModel mediaModel : mediaModels) {
            if (!moveToTrash(context, mediaModel)) {
                success = false;
            }
        }
        notifyMediaStoreScan(context, trashPath);
        GalleryDB.notifyMediaObservers();
        GalleryDB.notifyAlbumObservers();
        return success;
    }

    private boolean moveToTrash(Context context, MediaModel mediaModel) {
        try {
            String mediaPath = mediaModel.localPath;

            File sourceFile = new File(mediaPath);
            String destinationPath = trashPath + "/" + sourceFile.getName();
            File destinationFile = new File(destinationPath);

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            if (sourceFile.delete()) {
                try (GalleryDB db = new GalleryDB(context)) {
                    db.addToTrashTable(mediaModel, destinationPath);
                    db.removeFromMediaTable(new ArrayList<>(Collections.singletonList(mediaModel)));
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error trashing item");
            return false;
        }
    }

}
