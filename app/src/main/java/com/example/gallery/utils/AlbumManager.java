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

import com.example.gallery.utils.database.AlbumModel;
import com.example.gallery.utils.database.GalleryDB;
import com.example.gallery.utils.database.MediaModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class AlbumManager extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            // Handle intent
            String action = intent.getStringExtra("action");
            if(action == null) finish();
            if (action.equals("add")) {
                ArrayList<MediaModel> mediaModels = intent.getParcelableArrayListExtra("mediaModels");
                AlbumModel albumModel = intent.getParcelableExtra("albumModel");
                boolean result = moveMedia(this, mediaModels, albumModel);
                Log.d("AlbumManager", "Media moved: " + result);
                setResult(result ? RESULT_OK : RESULT_CANCELED);
                finish();
            }else if(action.equals("create")) {
                String albumName = intent.getStringExtra("albumName");
                AlbumModel albumModel = createNewAlbum(this, albumName);
                if(albumModel != null) {
                    setResult(RESULT_OK);
                    Log.d("AlbumManager", "Album created successfully");
                } else {
                    setResult(RESULT_CANCELED);
                    Log.d("AlbumManager", "Album failed to create");
                }
                finish();
            }

        }
        else {
            finish();
        }
    }

    public boolean moveMedia(Context context, ArrayList<MediaModel> mediaModels, AlbumModel albumModel) {
        boolean success = true;
        boolean applyToAll = false;
        for (MediaModel mediaModel : mediaModels) {
            if (!moveMedia(context, mediaModel, albumModel)) {
                success = false;
            }
        }
        notifyMediaStoreScan(context, albumModel.localPath);
        return success;
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
            String destinationPath = albumModel.localPath;
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

            mediaModel.localPath = destinationFile.getAbsolutePath();
            Log.d("AlbumManager", "File: " + mediaModel.localPath);
            Log.d("AlbumManager", "Album: " + albumModel.localPath);

            if (sourceFile.delete()) {
                GalleryDB db = new GalleryDB(context);
                db.updateMediaPath(mediaModel);
                return true;
            } else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AlbumManager", "Error moving file");
            return false; // Error occurred while moving file
        }
    }

    public static void notifyMediaStoreScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, (path, uri) -> {
            // MediaScannerConnection callback
        });
    }

    public AlbumModel createNewAlbum(@NonNull Context context, String albumName) {
        File album = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);

        if (!album.exists()) {
            if (album.mkdirs()) {
                // Return directory path
                String albumPath = album.getAbsolutePath();
                AlbumModel albumModel = new AlbumModel();
                albumModel.setAlbumName(albumName)
                        .setLocalPath(albumPath)
                        .setCreatedAt(System.currentTimeMillis());

                GalleryDB db = new GalleryDB(context);
                db.addToAlbumTable(new ArrayList<>(Collections.singletonList(albumModel)));

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
}
