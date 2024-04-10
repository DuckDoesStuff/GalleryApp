package com.example.gallery.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.gallery.R;
import com.example.gallery.activities.AuthActivity;
import com.example.gallery.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private String username, email, displayName;
    private FirebaseAuth auth;
    private ActivityResultLauncher<Intent> launcher;

    private MainActivity mainActivity;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) requireActivity();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if(result.getResultCode() == RESULT_OK) {
                        Log.d("Auth", "ProfileFragment: Logged in from AuthActivity");
                    }
                }
        );


        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Log.d("Auth", "ProfileFragment: Session already authenticated");
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", 0);
            username = sharedPreferences.getString("username", null);
            email = sharedPreferences.getString("user_email", null);
            displayName = sharedPreferences.getString("user_id", null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v;
        if(auth.getCurrentUser() != null) {
            v = inflater.inflate(R.layout.fragment_profile, container, false);
            TextView textView = v.findViewById(R.id.email);
            textView.setText(email);
        }else {
            v = inflater.inflate(R.layout.fragment_guest, container, false);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(auth.getCurrentUser() != null) {
            view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_session", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                auth.signOut();
            });
        }else {
            view.findViewById(R.id.btn_login_google).setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                launcher.launch(intent);
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        launcher.unregister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}