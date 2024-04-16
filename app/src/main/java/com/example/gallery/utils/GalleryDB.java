package com.example.gallery.utils;

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
    // START SCHEMES
    public static class AlbumScheme {
        public String albumName;
        public String albumPath;
        public String albumThumb;
        public boolean hidden;
        public String createdAt;
        public int albumCount;

        public AlbumScheme(String albumName, String albumPath, String albumThumb, int albumCount, @Nullable boolean hidden, @Nullable String createdAt) {
            this.albumName = albumName;
            this.albumPath = albumPath;
            this.albumThumb = albumThumb;
            this.albumCount = albumCount;
            this.hidden = hidden;
            if (createdAt == null) {
                this.createdAt = "CURRENT_TIMESTAMP";
            } else {
                this.createdAt = createdAt;
            }
        }
    }

    // END SCHEMES

    static volatile ArrayList<DatabaseObserver> observers = new ArrayList<>();

    public static void addObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer added");
        observers.add(observer);
    }

    public static void removeObserver(DatabaseObserver observer) {
        Log.d("GalleryDB", "Observer removed");
        observers.remove(observer);
    }

    public static void notifyObservers() {
        for (DatabaseObserver observer : observers) {
            observer.onDatabaseChanged();
            Log.d("GalleryDB", "Observer notified");
        }
    }

    public static final String DATABASE_NAME = "gallery_app.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TRASH_TABLE =
                    "CREATE TABLE IF NOT EXISTS trash (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "original_path TEXT NOT NULL," +
                    "trashed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    private static final String SQL_CREATE_ALBUM_TABLE =
                    "CREATE TABLE IF NOT EXISTS albums (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "album_name TEXT NOT NULL," +
                    "album_path TEXT NOT NULL UNIQUE," +
                    "album_thumbnail TEXT," +
                    "album_count INT DEFAULT 0," +
                    "hidden BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

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
                    "UNIQUE (local_path, cloud_path, media_id))";

    // START SQLITE HELPER
    public GalleryDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
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

    // END SQLITE HELPER


    // START TRASH
    public void onNewItemTrashed(String originalPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO trash (original_path) VALUES ('" + originalPath + "')");
        db.close();
        onRemoveImageToUpload(originalPath);
    }

    public void onItemRestored(String originalPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM trash WHERE original_path = '" + originalPath + "'");
        db.close();
    }
    public String getOriginalPath(String fileName) {
        String originalPath = null;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;

        try {
            // Truy vấn cơ sở dữ liệu để lấy original_path
            String[] projection = { "original_path" };
            String selection = "original_path LIKE ?";
            String[] selectionArgs = { "%" + fileName + "%" }; // Sử dụng phép toán LIKE để tìm kiếm phần của tên tệp tin

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

    // END TRASH


    // START ALBUM
    public ArrayList<AlbumScheme> getAlbums() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<AlbumScheme> albumSchemes = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM albums", null);
        while (cursor.moveToNext()) {
            String albumName = cursor.getString(cursor.getColumnIndexOrThrow("album_name"));
            String albumPath = cursor.getString(cursor.getColumnIndexOrThrow("album_path"));
            String albumThumb = cursor.getString(cursor.getColumnIndexOrThrow("album_thumbnail"));
            int albumCount = cursor.getInt(cursor.getColumnIndexOrThrow("album_count"));
            albumSchemes.add(new AlbumScheme(albumName, albumPath, albumThumb, albumCount, cursor.getInt(cursor.getColumnIndexOrThrow("hidden")) == 1, cursor.getString(cursor.getColumnIndexOrThrow("created_at"))));
        }
        cursor.close();
        db.close();
        return albumSchemes;
    }

    public void updateAlbums(ArrayList<AlbumScheme> albumSchemes) {
        SQLiteDatabase db = getWritableDatabase();

        for (AlbumScheme albumScheme : albumSchemes) {
            // Check if album with the same album_name already exists
            Cursor cursor = db.rawQuery("SELECT 1 FROM albums WHERE album_name = ?", new String[]{albumScheme.albumName});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            if (exists) {
                // Album with the same album_name already exists, update the row
                ContentValues values = new ContentValues();
                values.put("album_name", albumScheme.albumName);
                values.put("album_path", albumScheme.albumPath);
                values.put("album_thumbnail", albumScheme.albumThumb);
                values.put("album_count", albumScheme.albumCount);
                values.put("hidden", albumScheme.hidden ? 1 : 0);
                values.put("created_at", albumScheme.createdAt);

                db.update("albums", values, "album_name = ?", new String[]{albumScheme.albumName});
            } else {
                // Album does not exist, insert a new row
                ContentValues values = new ContentValues();
                values.put("album_name", albumScheme.albumName);
                values.put("album_path", albumScheme.albumPath);
                values.put("album_thumbnail", albumScheme.albumThumb);
                values.put("album_count", albumScheme.albumCount);
                values.put("hidden", albumScheme.hidden ? 1 : 0);
                values.put("created_at", albumScheme.createdAt);

                db.insert("albums", null, values);
            }
        }

        db.close();
    }

    public void onAlbumCreated(String albumName, String albumPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO albums (album_name, album_path) VALUES ('" + albumName + "', '" + albumPath + "')");
        db.close();
    }

    public void onAlbumHidden(String albumName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE albums SET hidden = TRUE WHERE album_name = '" + albumName + "'");
        db.close();
    }

    public void onAlbumUnhidden(String albumName) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE albums SET hidden = FALSE WHERE album_name = '" + albumName + "'");
        db.close();
    }

    // END ALBUM


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

    public void onRemoveImageToUpload(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM to_upload WHERE image_path = ?", new String[]{imagePath});
        db.close();
    }

    // END UPLOAD


    // START MEDIA
    public ArrayList<MediaModel> getAllMedia() {
        ArrayList<MediaModel> medialModels = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String []args;
        if (user != null) {
            args = new String[]{user.getUid()};
        }else {
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
                medialModels.add(new MediaModel()
                        .setLocalPath(localPath)
                        .setCloudPath(cloudPath)
                        .setDownloaded(downloaded)
                        .setAlbumName(albumName)
                        .setType(type)
                        .setDuration(duration)
                        .setGeoLocation(location)
                        .setMediaID(mediaID)
                        .setDateTaken(dateTaken));
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

    public void onNewMedia(String cloudPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO media (user_id, cloud_path) VALUES ('" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "', '" + cloudPath + "')");
        db.close();
    }

    public void addToMediaTable(ArrayList<MediaModel> mediaModels) {
        SQLiteDatabase db = getWritableDatabase();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";
        for (MediaModel mediaModel : mediaModels) {
            ContentValues values = getMediaValue(mediaModel, uid);
            db.insertWithOnConflict("media", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        }
        db.close();
        Log.d("GalleryDB", "Media table updated");
        notifyObservers();
    }

    public void removeFromMediaTable(ArrayList<MediaModel> mediaModels) {
        SQLiteDatabase db = getWritableDatabase();
        for (MediaModel mediaModel : mediaModels) {
            db.execSQL("DELETE FROM media WHERE media_id = " + mediaModel.mediaID);
        }
        db.close();
        Log.d("GalleryDB", "Media table updated");
        notifyObservers();
    }

    @NonNull
    private static ContentValues getMediaValue(MediaModel mediaModel, String uid) {
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
        return values;
    }

    // END MEDIA
}
