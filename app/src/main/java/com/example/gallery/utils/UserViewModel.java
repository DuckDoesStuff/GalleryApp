package com.example.gallery.utils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

public class UserViewModel extends ViewModel {
    private MutableLiveData<FirebaseUser> currentUser;

    public MutableLiveData<FirebaseUser> getCurrentUser() {
        if(currentUser == null) {
            currentUser = new MutableLiveData<>();
        }
        return currentUser;
    }
}
