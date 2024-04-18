package com.example.gallery.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.example.gallery.component.firebase.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserFragment extends Fragment {
    private FirebaseAuth auth;
    private FirebaseUser user;

    private MainActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        mainActivity = (MainActivity) requireActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, container, false);
        TextView textView = v.findViewById(R.id.email);
        textView.setText(user.getEmail());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            auth.signOut();
            UserViewModel userViewModel = mainActivity.userViewModel;
            userViewModel.getCurrentUser().setValue(null);
        });
    }

}
