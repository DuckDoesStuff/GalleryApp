package com.example.gallery.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
                    "CREATE TABLE IF NOT EXISTS images (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id," +
                    "local_path TEXT," +
                    "cloud_path TEXT," +
                    "type TEXT," +
                    "is_synced BOOLEAN DEFAULT(false)," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

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
        db.execSQL("DROP TABLE IF EXISTS images");
        onCreate(db);
    }

    // END SQLITE HELPER

    public void oneTimeExecution() {
        SQLiteDatabase db = getReadableDatabase();
        // Log everything in to_upload
        Cursor cursor = db.rawQuery("SELECT * FROM to_upload", null);
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
            Log.d("DB", "Image: " + path);
        }
        cursor.close();
        db.close();
    }


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

    public ArrayList<MediaModel> getImagesToUpload() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<MediaModel> images = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM to_upload", null);
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
            images.add(new MediaModel(path));
        }
        cursor.close();
        db.close();
        return images;
    }

    public void onRemoveImageToUpload(String imagePath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM to_upload WHERE image_path = ?", new String[]{imagePath});
        db.close();
    }

    // END UPLOAD

    // START IMAGE

    public void onNewImage(String cloudPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO images (user_id, cloud_path) VALUES ('" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "', '" + cloudPath + "')");
        db.close();
    }

    public void updateImages(Context context) {
        // This method will be called when the user logs in
        // It will sync the images table with the user firestore
        // If the image is not in the images table, it will be added

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        CollectionReference root = fs.collection(user.getUid());
        root.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot result = task.getResult();
                if (result == null) {
                    return;
                }

                SQLiteDatabase db = getWritableDatabase();
                for (int i = 0; i < result.size(); i++) {
                    String cloudPath = result.getDocuments().get(i).getId();
                    Cursor cursor = db.rawQuery("SELECT 1 FROM images WHERE cloud_path = ?", new String[]{cloudPath});
                    boolean exists = cursor.getCount() > 0;
                    cursor.close();
                    if (!exists) {
                        db.execSQL("INSERT INTO images (user_id, cloud_path) VALUES ('" + user.getUid() + "', '" + cloudPath + "')");
                    }
                }
            }
        });
    }

    public ArrayList<MediaModel> getImageFromCloud() {
        ArrayList<MediaModel> mediaModels = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM images", null);
        while (cursor.moveToNext()) {
            String cloudPath = cursor.getString(cursor.getColumnIndexOrThrow("cloud_path"));
            mediaModels.add(new MediaModel(cloudPath));
        }
        cursor.close();
        db.close();
        return mediaModels;
    }

    // END IMAGE
}
