package com.example.gallery.component;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class MediaViewModel extends ViewModel {
    private MutableLiveData<ArrayList<MediaModel>> mediaModel;
    private MutableLiveData<ArrayList<MediaModel>> selectedMedia;

    public MutableLiveData<ArrayList<MediaModel>> getMedia() {
        if (mediaModel == null) {
            mediaModel = new MutableLiveData<>();
        }
        return mediaModel;
    }

    public MutableLiveData<ArrayList<MediaModel>> getSelectedMedia() {
        if (selectedMedia == null) {
            selectedMedia = new MutableLiveData<>();
        }
        return selectedMedia;
    }
}
