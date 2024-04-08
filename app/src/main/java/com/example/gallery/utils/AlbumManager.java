package com.example.gallery.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AlbumManager {

    public static boolean moveMedia(Context context, String sourceFilePath, String destinationPath) {
        try {
            File sourceFile = new File(sourceFilePath);
            File destinationFile = new File(destinationPath + "/" + sourceFile.getName());

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
                notifyMediaStoreScan(context, destinationPath);
                return true;
            }else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error occurred while moving file
        }
    }

    private static void notifyMediaStoreScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, (path, uri) -> {
            // MediaScannerConnection callback
        });
    }

    public static String createNewAlbum(@NonNull Context context, String albumName) {
        File album = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), albumName);

        if (!album.exists()) {
            if (album.mkdirs()) {
                // Return directory path
                String albumPath = album.getAbsolutePath();
                GalleryDB db = new GalleryDB(context);
                db.onAlbumCreated(albumName, albumPath);
                return albumPath;
            }
        }
        return null; // Error occurred while creating album
    }
}
