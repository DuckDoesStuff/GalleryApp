package com.example.gallery.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;


public class GalleryDB extends SQLiteOpenHelper {
    // START SCHEMES
    public static class AlbumScheme {
        public String albumName;
        public String albumPath;
        public String albumThumb;
        public boolean hidden;
        public String createdAt;

        public AlbumScheme(String albumName, String albumPath, String albumThumb, @Nullable boolean hidden, @Nullable String createdAt) {
            this.albumName = albumName;
            this.albumPath = albumPath;
            this.albumThumb = albumThumb;
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
    private static final int DATABASE_VERSION = 2;

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
                    "hidden BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    // START SQLITE HELPER
    public GalleryDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRASH_TABLE);
        db.execSQL(SQL_CREATE_ALBUM_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trash");
        db.execSQL("DROP TABLE IF EXISTS albums");
        onCreate(db);
    }

    // END SQLITE HELPER

    public void oneTimeExecution(String SQL) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL);
        db.close();
    }


    // START TRASH
    public void onNewItemTrashed(String originalPath) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO trash (original_path) VALUES ('" + originalPath + "')");
        db.close();
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
            albumSchemes.add(new AlbumScheme(albumName, albumPath, albumThumb, cursor.getInt(cursor.getColumnIndexOrThrow("hidden")) == 1, cursor.getString(cursor.getColumnIndexOrThrow("created_at"))));
        }
        cursor.close();
        db.close();
        return albumSchemes;
    }

    public void updateAlbums(ArrayList<AlbumScheme> albumSchemes) {
        SQLiteDatabase db = getWritableDatabase();
        // Insert album with unique path
        for (AlbumScheme albumScheme : albumSchemes) {
            db.execSQL("INSERT OR REPLACE INTO albums (album_name, album_path, album_thumbnail, hidden, created_at) VALUES ('" + albumScheme.albumName + "', '" + albumScheme.albumPath + "','" + albumScheme.albumThumb + "', " + albumScheme.hidden + ", " + albumScheme.createdAt + ")");
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

}
