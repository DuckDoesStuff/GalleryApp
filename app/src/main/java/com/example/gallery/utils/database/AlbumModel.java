package com.example.gallery.utils.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AlbumModel implements Parcelable {
    public static final Creator<AlbumModel> CREATOR = new Creator<AlbumModel>() {
        @Override
        public AlbumModel createFromParcel(Parcel in) {
            return new AlbumModel(in);
        }

        @Override
        public AlbumModel[] newArray(int size) {
            return new AlbumModel[size];
        }
    };
    public String albumName = "";
    public String userID = "";
    public String localPath = "";
    public String cloudPath = "";
    public String albumThumbnail = "";
    public int mediaCount = 0;
    public boolean hidden = false;
    public boolean downloaded = false;
    public long createdAt = 0;

    public AlbumModel() {
    }

    protected AlbumModel(Parcel in) {
        albumName = in.readString();
        userID = in.readString();
        localPath = in.readString();
        cloudPath = in.readString();
        albumThumbnail = in.readString();
        mediaCount = in.readInt();
        hidden = in.readByte() != 0;
        downloaded = in.readByte() != 0;
        createdAt = in.readLong();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AlbumModel)) {
            return false;
        }
        AlbumModel albumModel = (AlbumModel) obj;
        return albumName.equals(albumModel.albumName) &&
                localPath.equals(albumModel.localPath) &&
                cloudPath.equals(albumModel.cloudPath);
    }

    public AlbumModel setAlbumName(String albumName) {
        this.albumName = albumName;
        return this;
    }

    public AlbumModel setUserID(String userID) {
        this.userID = userID;
        return this;
    }

    public AlbumModel setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public AlbumModel setCloudPath(String cloudPath) {
        this.cloudPath = cloudPath;
        return this;
    }

    public AlbumModel setAlbumThumbnail(String albumThumbnail) {
        this.albumThumbnail = albumThumbnail;
        return this;
    }

    public AlbumModel setMediaCount(int mediaCount) {
        this.mediaCount = mediaCount;
        return this;
    }

    public AlbumModel setHidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public AlbumModel setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
        return this;
    }

    public AlbumModel setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(albumName);
        dest.writeString(userID);
        dest.writeString(localPath);
        dest.writeString(cloudPath);
        dest.writeString(albumThumbnail);
        dest.writeInt(mediaCount);
        dest.writeByte((byte) (hidden ? 1 : 0));
        dest.writeByte((byte) (downloaded ? 1 : 0));
        dest.writeLong(createdAt);
    }
}
