package com.example.gallery.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class GalleryDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "gallery_app.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TRASH_TABLE =
            "CREATE TABLE IF NOT EXISTS trash (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "original_path TEXT NOT NULL," +
                    "trashed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    public GalleryDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRASH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

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
}
