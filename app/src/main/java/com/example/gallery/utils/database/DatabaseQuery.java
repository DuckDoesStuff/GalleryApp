package com.example.gallery.utils.database;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseQuery {
    public ArrayList<MediaModel> mediaModelsToAdd = new ArrayList<>();
    public ArrayList<AlbumModel> albumModelsToAdd = new ArrayList<>();
    public ArrayList<MediaModel> mediaModelsToDelete = new ArrayList<>();
    public ArrayList<AlbumModel> albumModelsToDelete = new ArrayList<>();
    Context context;

    public DatabaseQuery(Context context) {
        this.context = context;
    }

    public void queryMedia() {
        // Query the MediaStore for new media here
        ArrayList<MediaModel> mediaList = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATE_TAKEN,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.MIME_TYPE
        };

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );
        GalleryDB db = new GalleryDB(context);
        if (imageCursor != null) {
            try {
                while (imageCursor.moveToNext()) {
                    String albumName = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                    String type = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
                    String localPath = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    int mediaID = imageCursor.getInt(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    long dateTaken = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
                    if (dateTaken == 0) {
                        dateTaken = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    }
                    int duration = imageCursor.getInt(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DURATION));
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setBucketID(bucketID)
                            .setAlbumName(albumName)
                            .setType(type)
                            .setLocalPath(localPath)
                            .setMediaID(mediaID)
                            .setDateTaken(dateTaken)
                            .setDuration(duration);
                    mediaList.add(mediaModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("DatabaseQuery", "Error getting media");
            } finally {
                imageCursor.close();
            }
        }

        Cursor videoCursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.MediaColumns.DATE_TAKEN + " DESC"
        );
        if (videoCursor != null) {
            try {
                while (videoCursor.moveToNext()) {
                    String albumName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                    String bucketID = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID));
                    String type = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
                    String localPath = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    int mediaID = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    long dateTaken = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN));
                    if (dateTaken == 0) {
                        dateTaken = videoCursor.getLong(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                    }
                    int duration = videoCursor.getInt(videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
                    MediaModel mediaModel = new MediaModel();
                    mediaModel.setBucketID(bucketID)
                            .setAlbumName(albumName)
                            .setType(type)
                            .setLocalPath(localPath)
                            .setMediaID(mediaID)
                            .setDateTaken(dateTaken)
                            .setDuration(duration);
                    mediaList.add(mediaModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("DatabaseQuery", "Error getting media");
            } finally {
                videoCursor.close();
            }
        }


        ArrayList<MediaModel> dbAllMedia = db.getAllMedia();

        // Check for new media
        mediaModelsToAdd = new ArrayList<>();
        mediaModelsToAdd =
            mediaList
            .stream()
            .filter(mediaModel -> !dbAllMedia.contains(mediaModel))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // Check for deleted media
        mediaModelsToDelete = new ArrayList<>();
        mediaModelsToDelete =
            dbAllMedia
            .stream()
            .filter(mediaModel -> !mediaList.contains(mediaModel))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void addToMediaTable(ArrayList<MediaModel> mediaModels) {
        // Update SQLite database with the retrieved information here
        try (GalleryDB galleryDB = new GalleryDB(context)) {
            // Update the database
            galleryDB.addToMediaTable(mediaModels);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void removeFromMediaTable(ArrayList<MediaModel> mediaModels) {
        // Update SQLite database with the retrieved information here
        try (GalleryDB galleryDB = new GalleryDB(context)) {
            // Update the database
            galleryDB.removeFromMediaTable(mediaModels);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryAlbum() {
        ArrayList<AlbumModel> albumModels = new ArrayList<>();

        String[] projection = {
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATA
        };

        Cursor imageCursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_TAKEN + " ASC"
        );

        if (imageCursor != null) {
            try {
                while (imageCursor.moveToNext()) {
                    String albumName = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String mediaPath = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String localPath = mediaPath.substring(0, mediaPath.lastIndexOf('/'));
                    AlbumModel albumModel = new AlbumModel();
                    albumModel.setAlbumName(albumName)
                            .setLocalPath(localPath)
                            .setAlbumThumbnail(mediaPath)
                            .setHidden(false)
                            .setDownloaded(false)
                            .setCreatedAt(System.currentTimeMillis());
                    if (!albumModels.contains(albumModel)) {
                        albumModels.add(albumModel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("DatabaseQuery", "Error getting album");
            } finally {
                imageCursor.close();
            }
            imageCursor.close();
        }

        Cursor videoCursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_TAKEN + " ASC"
        );

        if (videoCursor != null) {
            try {
                while (videoCursor.moveToNext()) {
                    String albumName = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    String mediaPath = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String localPath = mediaPath.substring(0, mediaPath.lastIndexOf('/'));
                    AlbumModel albumModel = new AlbumModel();
                    albumModel.setAlbumName(albumName)
                            .setLocalPath(localPath)
                            .setAlbumThumbnail(mediaPath)
                            .setHidden(false)
                            .setDownloaded(false)
                            .setCreatedAt(System.currentTimeMillis());
                    if (!albumModels.contains(albumModel))
                        albumModels.add(albumModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("DatabaseQuery", "Error getting album");
            } finally {
                videoCursor.close();
            }
            videoCursor.close();
        }

        try (GalleryDB db = new GalleryDB(context)) {
            ArrayList<AlbumModel> dbAlbums = db.getAllAlbums();
            albumModelsToAdd = new ArrayList<>();
            albumModelsToAdd = albumModels.stream().filter(albumModel -> !dbAlbums.contains(albumModel)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            albumModelsToDelete = new ArrayList<>();
            albumModelsToDelete = dbAlbums.stream().filter(albumModel -> !albumModels.contains(albumModel)).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToAlbumTable(ArrayList<AlbumModel> albumModels) {
        try (GalleryDB db = new GalleryDB(context)) {
            db.addToAlbumTable(albumModels);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFromAlbumTable(ArrayList<AlbumModel> albumModels) {
        try (GalleryDB db = new GalleryDB(context)) {
            db.removeFromAlbumTable(albumModels);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
