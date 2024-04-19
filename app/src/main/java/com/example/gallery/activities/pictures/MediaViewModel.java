package com.example.gallery.activities.pictures;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gallery.utils.database.MediaModel;

import java.util.ArrayList;

public class MediaViewModel extends ViewModel {
    private MutableLiveData<ArrayList<MediaModel>> mediaModel;
    private MutableLiveData<ArrayList<Integer>> selectedMedia;

    public MutableLiveData<ArrayList<MediaModel>> getMedia() {
        if (mediaModel == null) {
            mediaModel = new MutableLiveData<>();
        }
        return mediaModel;
    }

    public MediaModel getMedia(int index) {
        if (mediaModel == null || mediaModel.getValue() == null) {
            return null;
        }
        return mediaModel.getValue().get(index);
    }

    public MutableLiveData<ArrayList<Integer>> getSelectedMedia() {
        if (selectedMedia == null) {
            selectedMedia = new MutableLiveData<>();
        }
        return selectedMedia;
    }

    public void setSelectedMedia(Integer index) {
        if (selectedMedia == null) {
            selectedMedia = new MutableLiveData<>();
        }
        ArrayList<Integer> selected = selectedMedia.getValue();
        if (selected == null) {
            selected = new ArrayList<>();
        }
        if (selected.contains(index)) {
            selected.remove(index);
        } else {
            selected.add(index);
        }
        selectedMedia.setValue(selected);
    }

    public void clearSelectedMedia() {
        if (selectedMedia == null) {
            selectedMedia = new MutableLiveData<>();
        }
        selectedMedia.setValue(new ArrayList<>());
    }

    public boolean isSelected(int index) {
        if (selectedMedia == null || selectedMedia.getValue() == null) {
            return false;
        }
        return selectedMedia.getValue().contains(index);
    }
}
