package com.example.gallery.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gallery.R;
import com.example.gallery.activities.AuthActivity;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.utils.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class GuestFragment extends Fragment {
    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> launcher;

    private MainActivity mainActivity;
    public GuestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if(result.getResultCode() == RESULT_OK) {
                        Log.d("Auth", "GuestFragment: Logged in from AuthActivity");
                        UserViewModel userViewModel = mainActivity.userViewModel;
                        userViewModel.getCurrentUser().setValue(auth.getCurrentUser());
                    }
                }
        );

        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_guest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_login_google).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            launcher.launch(intent);
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
