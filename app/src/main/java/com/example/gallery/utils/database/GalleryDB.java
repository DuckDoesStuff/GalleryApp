package com.example.gallery.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;


public class GalleryDB extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "gallery_app.db";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_FACE_TABLE =
            "CREATE TABLE IF NOT EXISTS face (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "media_id TEXT)";
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

    static volatile ArrayList<DatabaseObserver> faceObservers = new ArrayList<>();



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
        ContentValues values = new ContentValues();
        values.put("user_id", "");
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

    private MediaModel getMediaModelFromCursor(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);

        int mediaID = values.getAsInteger("media_id");
        String localPath = values.getAsString("local_path");
        String cloudPath = values.getAsString("cloud_path");
        boolean downloaded = values.getAsBoolean("downloaded");
        String albumName = values.getAsString("album_name");
        String type = values.getAsString("type");
        int duration = values.getAsInteger("duration");
        String location = values.getAsString("location");
        long dateTaken = values.getAsLong("date_taken");
        boolean favorite = values.getAsBoolean("favorite");

        return new MediaModel()
                .setLocalPath(localPath)
                .setCloudPath(cloudPath)
                .setDownloaded(downloaded)
                .setAlbumName(albumName)
                .setType(type)
                .setDuration(duration)
                .setGeoLocation(location)
                .setMediaID(mediaID)
                .setDateTaken(dateTaken)
                .setFavorite(favorite);
    }

    private AlbumModel getAlbumModelFromCursor(Cursor cursor) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);

        String userID = values.getAsString("user_id");
        String albumName = values.getAsString("album_name");
        String localPath = values.getAsString("local_path");
        String cloudPath = values.getAsString("cloud_path");
        String albumThumb = values.getAsString("album_thumb");
        boolean hidden = values.getAsBoolean("hidden");
        boolean downloaded = values.getAsBoolean("downloaded");
        long createdAt = values.getAsLong("created_at");

        return new AlbumModel()
                .setUserID(userID)
                .setAlbumName(albumName)
                .setLocalPath(localPath)
                .setCloudPath(cloudPath)
                .setAlbumThumbnail(albumThumb)
                .setHidden(hidden)
                .setDownloaded(downloaded)
                .setCreatedAt(createdAt);
    }
    // END SQLITE HELPER

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FACE_TABLE);
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
        db.execSQL("DROP TABLE IF EXISTS face");

        onCreate(db);
    }

    // START TRASH
    public ArrayList<MediaModel> getAllTrash() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM trash", null);
        while (cursor.moveToNext()) {
            String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
            String localPath = cursor.getString(cursor.getColumnIndexOrThrow("local_path"));
            String trashPath = cursor.getString(cursor.getColumnIndexOrThrow("trash_path"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            mediaModels.add(new MediaModel().setTrashPath(trashPath).setCloudPath(cloudPath).setType(type).setLocalPath(localPath));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }

    public ArrayList<MediaModel> getAllFavorite() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM media WHERE favorite = 1", null);
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
                mediaModels.add(new MediaModel()
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
        return mediaModels;
    }
    public ArrayList<MediaModel> getAllFace() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM face", null);
        while (cursor.moveToNext()) {
            String localPath = cursor.getString(cursor.getColumnIndexOrThrow("local_path"));
            mediaModels.add(new MediaModel().setLocalPath(localPath));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }
    public void clearFaceTable() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("face", null, null);
        db.close();
    }
    public void addToFaceTable(String localPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("local_path", localPath);
        db.insertWithOnConflict("face", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
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
        db.execSQL("DELETE FROM trash WHERE trash_path = '" + mediaModel.trashPath + "'");
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
            AlbumModel albumModel = getAlbumModelFromCursor(cursor);

            Cursor getMediaCount = db.rawQuery("SELECT COUNT(*) FROM media WHERE album_name = ?", new String[]{albumModel.albumName});
            int mediaCount = 0;
            if (getMediaCount.moveToFirst()) {
                mediaCount = getMediaCount.getInt(0);
            }
            getMediaCount.close();

            albumSchemes.add(albumModel.setMediaCount(mediaCount));
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
            mediaModels.add(getMediaModelFromCursor(cursor).setAlbumName(albumModel.albumName));
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

    // END ALBUM


    // START UPLOAD
    public ArrayList<MediaModel> getNotSynced() {
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM media WHERE user_id = '' OR user_id = NULL OR (local_path != '' AND cloud_path = '')", null);
        while (cursor.moveToNext()) {
            mediaModels.add(getMediaModelFromCursor(cursor));
        }
        return mediaModels;
    }

    // END UPLOAD

    // START MEDIA
    public ArrayList<MediaModel> getAllLocalMedia() {
        ArrayList<MediaModel> medialModels = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM media WHERE local_path != NULL or local_path != ''",
                null);

        try {
            while (cursor.moveToNext()) {
                medialModels.add(getMediaModelFromCursor(cursor));
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

    public ArrayList<MediaModel> getAllCloudOnlyMedia() {
        ArrayList<MediaModel> medialModels = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return medialModels;
        }
        String userID = user.getUid();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM media WHERE (cloud_path != NULL or cloud_path != '') " +
                    "AND (local_path = NULL or local_path = '') AND user_id = ?",
                new String[]{userID});

        while(cursor.moveToNext()) {
            medialModels.add(getMediaModelFromCursor(cursor));
        }

        return medialModels;
    }

    public void updateMedia(MediaModel mediaModel) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = getMediaValue(mediaModel);
        int result = db.updateWithOnConflict("media", values, "media_id = ?", new String[]{String.valueOf(mediaModel.mediaID)}, SQLiteDatabase.CONFLICT_IGNORE);
        Log.d("GalleryDB", "Media updated: " + result);
        db.close();
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
    public ArrayList<MediaModel> getSearchMedia(String searchInputText) {
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        if(searchInputText.isEmpty())
            return mediaModels;
        SQLiteDatabase db = getReadableDatabase();


        List<String> selectionArgs = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM media WHERE album_name LIKE ?");

        selectionArgs.add("%" + searchInputText + "%");



        Cursor cursor = db.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));

        try {
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
                        .setAlbumName(cursor.getString(cursor.getColumnIndexOrThrow("album_name")))
                        .setType(type)
                        .setDuration(duration)
                        .setGeoLocation(location)
                        .setMediaID(mediaID)
                        .setDateTaken(dateTaken));
            }
        } catch (Exception e) {
            Log.e("GalleryDB", "Error while fetching media from database", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return mediaModels;
    }
    // END MEDIA
}
