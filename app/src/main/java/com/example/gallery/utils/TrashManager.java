package com.example.gallery.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.example.gallery.utils.database.GalleryDB;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TrashManager {

    private static String trashPath;

    private static void notifyMediaStoreScan(Context context, String filePath) {
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, (path, uri) -> {
            // MediaScannerConnection callback
        });
    }

    public static ArrayList<String> getFilesFromTrash() {
        File trashDirectory = new File(trashPath);
        File[] files = trashDirectory.listFiles();
        ArrayList<String> output = new ArrayList<>();
        for (File file : files) {
            output.add(file.getAbsolutePath());
        }
        return output;
    }

    public static void createTrash() {
        File album = new File(android.os.Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), ".trash");
        if (!album.exists()) {
            album.mkdirs();
        }
        trashPath = album.getAbsolutePath();
    }

    public static boolean moveToTrash(@NonNull Context context, String imgPath) {
        GalleryDB db = new GalleryDB(context);
        db.onNewItemTrashed(imgPath);
        try {
            File sourceFile = new File(imgPath);
            File destinationFile = new File(trashPath + "/" + sourceFile.getName());

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
                notifyMediaStoreScan(context, trashPath);
                return true;
            } else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error occurred while moving file
        }
    }

    public static void deleteFromTrash(String trashFilePath) {
        // Tạo đối tượng File từ đường dẫn tệp trong thư mục .trash
        File trashFile = new File(trashFilePath);

        // Kiểm tra xem tệp tồn tại trước khi xóa
        if (trashFile.exists()) {
            // Xóa tệp từ thư mục .trash
            boolean deleted = trashFile.delete();
        }
    }

    public static boolean restoreFromTrash(@NonNull Context context, String imgName, String imgPath) {
        GalleryDB db = new GalleryDB(context);
        String path = db.getOriginalPath(imgName);

        try {
            File sourceFile = new File(imgPath);
            File destinationFile = new File(path);

            FileInputStream inputStream = new FileInputStream(sourceFile);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            db.onItemRestored(path);

            inputStream.close();
            outputStream.close();

            if (sourceFile.delete()) {
                notifyMediaStoreScan(context, trashPath);
                return true;
            } else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Error occurred while moving file
        }
    }
}
