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
    public String albumName = "";
    public String bucketID = "";
    public String type = "";
    public String localPath = "";
    public String cloudPath = "";
    public String geoLocation = "";
    public int mediaID = 0;
    public long dateTaken = 0;
    public int duration = 0;
    public boolean downloaded = false;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaModel) {
            MediaModel mediaModel = (MediaModel) obj;
            return mediaModel.mediaID == mediaID;
        }
        return false;
    }

    public MediaModel() {
    }

    public MediaModel setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public MediaModel setBucketID(String bucketID) {
        this.bucketID = bucketID;
        return this;
    }

    public MediaModel setType(String type) {
        this.type = type;
        return this;
    }

    public MediaModel setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public MediaModel setCloudPath(String cloudPath) {
        this.cloudPath = cloudPath;
        return this;
    }

    public MediaModel setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
        return this;
    }

    public MediaModel setMediaID(int mediaID) {
        this.mediaID = mediaID;
        return this;
    }

    public MediaModel setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
        return this;
    }

    public MediaModel setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public MediaModel setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
        return this;
    }

    // Parcelable implementation
    protected MediaModel(Parcel in) {
        albumName = in.readString();
        bucketID = in.readString();
        type = in.readString();
        localPath = in.readString();
        cloudPath = in.readString();
        geoLocation = in.readString();
        mediaID = in.readInt();
        dateTaken = in.readLong();
        duration = in.readInt();
        downloaded = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeString(bucketID);
        dest.writeString(type);
        dest.writeString(localPath);
        dest.writeString(cloudPath);
        dest.writeString(geoLocation);
        dest.writeInt(mediaID);
        dest.writeLong(dateTaken);
        dest.writeInt(duration);
        dest.writeByte((byte) (downloaded ? 1 : 0));
    }

}
