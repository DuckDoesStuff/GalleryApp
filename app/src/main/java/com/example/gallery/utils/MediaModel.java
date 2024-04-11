package com.example.gallery.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MediaModel implements Parcelable {
    public static final Creator<MediaModel> CREATOR = new Creator<MediaModel>() {
        @Override
        public MediaModel createFromParcel(Parcel in) {
            return new MediaModel(in);
        }

        @Override
        public MediaModel[] newArray(int size) {
            return new MediaModel[size];
        }
    };
    public String albumName;
    public String bucketID;
    public String path;
    public String name;
    public String dateTaken;
    public String dateAdded;
    public String duration;
    public String type;
    public boolean isLocal = false;
    public boolean isSynced = false;


    public MediaModel(String albumName, String bucketID, String path, String name, String dateTaken, String dateAdded, String duration, String type) {
        this.albumName = albumName;
        this.bucketID = bucketID;
        this.path = path;
        this.name = name;
        this.dateTaken = dateTaken;
        this.dateAdded = dateAdded;
        this.duration = duration;
        this.type = type;
    }

    // Parcelable implementation
    protected MediaModel(Parcel in) {
        albumName = in.readString();
        bucketID = in.readString();
        path = in.readString();
        name = in.readString();
        type = in.readString();
        dateTaken = in.readString();
        dateAdded = in.readString();
        duration = in.readString();
        isLocal = in.readBoolean();
        isSynced = in.readBoolean();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeString(bucketID);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(dateTaken);
        dest.writeString(dateAdded);
        dest.writeString(duration);
        dest.writeBoolean(isLocal);
        dest.writeBoolean(isSynced);
    }

}
