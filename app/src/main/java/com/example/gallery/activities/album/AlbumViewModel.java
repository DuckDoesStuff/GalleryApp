package com.example.gallery.activities.album;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gallery.utils.database.AlbumModel;

import java.util.ArrayList;

public class AlbumViewModel extends ViewModel {
    private MutableLiveData<ArrayList<AlbumModel>> albumModels;
    private MutableLiveData<ArrayList<AlbumModel>> selectedAlbums;

    public MutableLiveData<ArrayList<AlbumModel>> getAlbums() {
        if (albumModels == null) {
            albumModels = new MutableLiveData<>();
        }
        return albumModels;
    }

    public AlbumModel getAlbum(int index) {
        if (albumModels == null || albumModels.getValue() == null) {
            return null;
        }
        return albumModels.getValue().get(index);
    }

    public MutableLiveData<ArrayList<AlbumModel>> getSelectedAlbums() {
        if (selectedAlbums == null) {
            selectedAlbums = new MutableLiveData<>();
        }
        return selectedAlbums;
    }
}