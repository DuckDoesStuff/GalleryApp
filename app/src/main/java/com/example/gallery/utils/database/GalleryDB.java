package com.example.gallery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class GalleryDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "gallery_app.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_TRASH_TABLE =
            "CREATE TABLE IF NOT EXISTS trash (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "local_path TEXT," +
                    "cloud_path TEXT," +
                    "trash_path TEXT," +
                    "type TEXT," +
                    "trashed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    private static final String SQL_CREATE_ALBUM_TABLE =
            "CREATE TABLE IF NOT EXISTS albums (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id TEXT," +
                    "album_name TEXT," +
                    "local_path TEXT," +
                    "cloud_path TEXT," +
                    "album_thumb TEXT," +
                    "hidden BOOLEAN DEFAULT FALSE," +
                    "downloaded BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE (album_name, local_path, cloud_path))";
    private static final String SQL_CREATE_TO_UPLOAD_TABLE =
            "CREATE TABLE IF NOT EXISTS to_upload (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "image_path TEXT NOT NULL UNIQUE)";
    // This table will be synced with the user firestore
    private static final String SQL_CREATE_MEDIA_TABLE =
            "CREATE TABLE IF NOT EXISTS media (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "media_id TEXT," +
                    "user_id TEXT," +
                    "local_path TEXT," +
                    "cloud_path TEXT," +
                    "downloaded BOOLEAN," +
                    "album_name TEXT," +
                    "type TEXT," +
                    "duration INTEGER," +
                    "location TEXT," +
                    "date_taken INTEGER," +
                    "favorite BOOLEAN," +
                    "UNIQUE (local_path, cloud_path, media_id))";
    // START observers
    static volatile ArrayList<DatabaseObserver> mediaObservers = new ArrayList<>();
    static volatile ArrayList<DatabaseObserver> albumObservers = new ArrayList<>();
    static volatile ArrayList<DatabaseObserver> trashObservers = new ArrayList<>();

    // END observers

    // START SQLITE HELPER
    public GalleryDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void addMediaObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer added");
        mediaObservers.add(observer);
    }

    public static void removeMediaObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer removed");
        mediaObservers.remove(observer);
    }

    public static void notifyMediaObservers() {
        for (DatabaseObserver observer : mediaObservers) {
            observer.onDatabaseChanged();
            Log.d("GalleryDB", "Observer notified");
        }
    }

    public static void addAlbumObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer added");
        albumObservers.add(observer);
    }

    public static void removeAlbumObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer removed");
        albumObservers.remove(observer);
    }

    public static void notifyAlbumObservers() {
        for (DatabaseObserver observer : albumObservers) {
            observer.onDatabaseChanged();
            Log.d("GalleryDB", "Observer notified");
        }
    }

    public static void addTrashObservers(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer added");
        trashObservers.add(observer);
    }

    public static void removeTrashObservers(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer removed");
        trashObservers.remove(observer);
    }

    public static void notifyTrashObservers() {
        for (DatabaseObserver observer : trashObservers) {
            observer.onDatabaseChanged();
            Log.d("GalleryDB", "Observer notified");
        }
    }

    @NonNull
    private static ContentValues getAlbumValues(AlbumModel albumModel) {
        ContentValues values = new ContentValues();
        values.put("user_id", albumModel.userID);
        values.put("album_name", albumModel.albumName);
        values.put("local_path", albumModel.localPath);
        values.put("cloud_path", albumModel.cloudPath);
        values.put("album_thumb", albumModel.albumThumbnail);
        values.put("hidden", albumModel.hidden ? 1 : 0);
        values.put("downloaded", albumModel.downloaded ? 1 : 0);
        values.put("created_at", albumModel.createdAt);
        return values;
    }

    @NonNull
    private static ContentValues getMediaValue(MediaModel mediaModel) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";
        ContentValues values = new ContentValues();
        values.put("user_id", uid);
        values.put("local_path", mediaModel.localPath);
        values.put("cloud_path", mediaModel.cloudPath);
        values.put("downloaded", mediaModel.downloaded ? 1 : 0);
        values.put("album_name", mediaModel.albumName);
        values.put("type", mediaModel.type);
        values.put("duration", mediaModel.duration);
        values.put("location", mediaModel.geoLocation);
        values.put("media_id", mediaModel.mediaID);
        values.put("date_taken", mediaModel.dateTaken);
        values.put("favorite", mediaModel.favorite);
        return values;
    }

    // END SQLITE HELPER

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRASH_TABLE);
        db.execSQL(SQL_CREATE_ALBUM_TABLE);
        db.execSQL(SQL_CREATE_TO_UPLOAD_TABLE);
        db.execSQL(SQL_CREATE_MEDIA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trash");
        db.execSQL("DROP TABLE IF EXISTS albums");
        db.execSQL("DROP TABLE IF EXISTS to_upload");
        db.execSQL("DROP TABLE IF EXISTS media");
        onCreate(db);
    }

    // START TRASH
    public ArrayList<MediaModel> getAllTrash() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM trash", null);
        while (cursor.moveToNext()) {
            String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
            String trashPath = cursor.getString(cursor.getColumnIndexOrThrow("trash_path"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            mediaModels.add(new MediaModel().setLocalPath(trashPath).setCloudPath(cloudPath).setType(type));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }

    public ArrayList<MediaModel> getAllFavorite() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM media WHERE favorite = 1", null);

        cursor.close();
        db.close();
        return mediaModels;
    }
    public void addToTrashTable(MediaModel mediaModel, String trashPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("local_path", mediaModel.localPath);
        values.put("cloud_path", mediaModel.cloudPath);
        values.put("trash_path", trashPath);
        values.put("type", mediaModel.type);
        db.insertWithOnConflict("trash", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeFromTrashTable(MediaModel mediaModel) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM trash WHERE original_path = '" + mediaModel.localPath + "'");
        db.close();
    }

    // END TRASH

    public String getOriginalPath(String fileName) {
        String originalPath = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;

        try {
            // Truy vấn cơ sở dữ liệu để lấy original_path
            String[] projection = {"original_path"};
            String selection = "original_path LIKE ?";
            String[] selectionArgs = {"%" + fileName + "%"}; // Sử dụng phép toán LIKE để tìm kiếm phần của tên tệp tin

            cursor = db.query(
                    "trash", // Tên bảng trong cơ sở dữ liệu
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            // Di chuyển con trỏ tới hàng đầu tiên (nếu có)
            if (cursor.moveToFirst()) {
                originalPath = cursor.getString(cursor.getColumnIndexOrThrow("original_path"));
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ (nếu có)
            e.printStackTrace();
        } finally {
            // Đóng con trỏ và cơ sở dữ liệu
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return originalPath;
    }

    public void onItemDeleted(String originalPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM trash WHERE original_path = '" + originalPath + "'");
        db.close();
    }

    // START ALBUM
    public ArrayList<AlbumModel> getAllAlbums() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<AlbumModel> albumSchemes = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM albums", null);
        while (cursor.moveToNext()) {
            String userID = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            String albumName = cursor.getString(cursor.getColumnIndexOrThrow("album_name"));
            String localPath = cursor.getString(cursor.getColumnIndexOrThrow("local_path"));
            String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
            String albumThumb = cursor.getString(cursor.getColumnIndexOrThrow("album_thumb"));
            boolean hidden = cursor.getInt(cursor.getColumnIndexOrThrow("hidden")) == 1;
            boolean downloaded = cursor.getInt(cursor.getColumnIndexOrThrow("downloaded")) == 1;
            long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));

            Cursor getMediaCount = db.rawQuery("SELECT COUNT(*) FROM media WHERE album_name = ?", new String[]{albumName});
            int mediaCount = 0;
            if (getMediaCount.moveToFirst()) {
                mediaCount = getMediaCount.getInt(0);
            }
            getMediaCount.close();


            albumSchemes.add(new AlbumModel()
                    .setAlbumName(albumName)
                    .setUserID(userID)
                    .setLocalPath(localPath)
                    .setCloudPath(cloudPath)
                    .setMediaCount(mediaCount)
                    .setAlbumThumbnail(albumThumb)
                    .setHidden(hidden)
                    .setDownloaded(downloaded)
                    .setCreatedAt(createdAt));
        }
        cursor.close();
        db.close();
        return albumSchemes;
    }

    public ArrayList<MediaModel> getMediaInAlbum(AlbumModel albumModel) {
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM media WHERE album_name = ?", new String[]{albumModel.albumName});
        while (cursor.moveToNext()) {
            String localPath = cursor.getString(cursor.getColumnIndexOrThrow("local_path"));
            String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
            boolean downloaded = cursor.getInt(cursor.getColumnIndexOrThrow("downloaded")) == 1;
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            int mediaID = cursor.getInt(cursor.getColumnIndexOrThrow("media_id"));
            long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow("date_taken"));
            mediaModels.add(new MediaModel()
                    .setLocalPath(localPath)
                    .setCloudPath(cloudPath)
                    .setDownloaded(downloaded)
                    .setAlbumName(albumModel.albumName)
                    .setType(type)
                    .setDuration(duration)
                    .setGeoLocation(location)
                    .setMediaID(mediaID)
                    .setDateTaken(dateTaken));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }

    public void updateAlbum(AlbumModel albumModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = getAlbumValues(albumModel);
        int result = db.updateWithOnConflict("albums", values, "local_path = ?", new String[]{albumModel.localPath}, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d("GalleryDB", "Album updated: " + result);
        db.close();
    }

    public void addToAlbumTable(ArrayList<AlbumModel> albumModels) {
        SQLiteDatabase db = getWritableDatabase();
        for (AlbumModel albumModel : albumModels) {
            ContentValues values = getAlbumValues(albumModel);
            db.insertWithOnConflict("albums", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.close();
        Log.d("GalleryDB", "Album table updated");
    }

    public void removeFromAlbumTable(ArrayList<AlbumModel> albumModels) {
    // END ALBUM

        // Not sure how to handle this yet
    }

    // START UPLOAD
    public void onNewImageToUpload(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT OR IGNORE INTO to_upload (image_path) VALUES ('" + imagePath + "')");
        db.close();
    }

    public ArrayList<MediaModel> getMediaToUpload() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM to_upload", null);
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
            mediaModels.add(new MediaModel().setLocalPath(path));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }

    // END UPLOAD

    public void onRemoveImageToUpload(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM to_upload WHERE image_path = ?", new String[]{imagePath});
        db.close();
    }

    // START MEDIA
    public ArrayList<MediaModel> getAllMedia() {
        ArrayList<MediaModel> medialModels = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String[] args;
        if (user != null) {
            args = new String[]{user.getUid()};
        } else {
            args = new String[]{""};
        }
        Cursor cursor = db.rawQuery(
                "SELECT * FROM media WHERE user_id = ? OR user_id = NULL",
                args);

        try {
            while (cursor.moveToNext()) {
                String localPath = cursor.getString(cursor.getColumnIndexOrThrow("local_path"));
                String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
                boolean downloaded = cursor.getInt(cursor.getColumnIndexOrThrow("downloaded")) == 1;
                String albumName = cursor.getString(cursor.getColumnIndexOrThrow("album_name"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow("duration"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                int mediaID = cursor.getInt(cursor.getColumnIndexOrThrow("media_id"));
                long dateTaken = cursor.getLong(cursor.getColumnIndexOrThrow("date_taken"));
                boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow("favorite")) == 1;
                Log.d("GalleryDB", "Favorite: " + favorite);
                medialModels.add(new MediaModel()
                        .setLocalPath(localPath)
                        .setCloudPath(cloudPath)
                        .setDownloaded(downloaded)
                        .setAlbumName(albumName)
                        .setType(type)
                        .setDuration(duration)
                        .setGeoLocation(location)
                        .setMediaID(mediaID)
                        .setDateTaken(dateTaken)
                        .setFavorite(favorite));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("GalleryDB", "Error while fetching media from database");
        } finally {
            cursor.close();
            db.close();
        }
        return medialModels;
    }

    // Pending for removal
    public void onNewMedia(String cloudPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO media (user_id, cloud_path) VALUES ('" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "', '" + cloudPath + "')");
        db.close();
    }

    public void updateMedia(MediaModel mediaModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = getMediaValue(mediaModel);
        int result = db.updateWithOnConflict("media", values, "media_id = ?", new String[]{String.valueOf(mediaModel.mediaID)}, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d("GalleryDB", "Media updated: " + result);
        db.close();
    }

    public boolean getFavorite(MediaModel mediaModel) {
        SQLiteDatabase db = getWritableDatabase();
        boolean favorite = false;

        // Thực hiện truy vấn SQL để lấy giá trị favorite từ cơ sở dữ liệu
        Cursor cursor = db.rawQuery("SELECT favorite FROM media WHERE media_id = ?", new String[]{String.valueOf(mediaModel.mediaID)});

        // Kiểm tra xem có dữ liệu được trả về không
        if (cursor != null && cursor.moveToFirst()) {
            // Lấy giá trị favorite từ cột đầu tiên của con trỏ
            favorite = cursor.getInt(0) == 1; // 1 nếu favorite là true, 0 nếu không
            cursor.close(); // Đóng con trỏ sau khi sử dụng
        }

        db.close(); // Đóng kết nối đến cơ sở dữ liệu

        // Trả về giá trị favorite
        return favorite;
    }

    public void addToMediaTable(ArrayList<MediaModel> mediaModels) {
        SQLiteDatabase db = getWritableDatabase();
        for (MediaModel mediaModel : mediaModels) {
            ContentValues values = getMediaValue(mediaModel);
            db.insertWithOnConflict("media", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.close();
        Log.d("GalleryDB", "Media table updated");
    }

    public void removeFromMediaTable(ArrayList<MediaModel> mediaModels) {
        SQLiteDatabase db = getWritableDatabase();
        for (MediaModel mediaModel : mediaModels) {
            db.execSQL("DELETE FROM media WHERE media_id = " + mediaModel.mediaID);
        }
        db.close();
        Log.d("GalleryDB", "Media table updated");
    }

    // END MEDIA
}
