package com.example.gallery.component.firebase;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gallery.R;
import com.example.gallery.activities.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser user;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;
    private MainActivity mainActivity;
    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                        .addOnSuccessListener(googleSignInAccount -> {
                            Log.d("SignIn", "signInWithGoogle:success");
                            SharedPreferences sharedPreferences = getSharedPreferences("user_session", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_id", googleSignInAccount.getId());
                            editor.putString("user_email", googleSignInAccount.getEmail());
                            editor.putString("username", googleSignInAccount.getDisplayName());
                            editor.apply();
                            //Log.d("SignIn", googleSignInAccount.getId() + " " + googleSignInAccount.getEmail() + " " + googleSignInAccount.getIdToken());
                            AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                            auth.signInWithCredential(credential)
                                    .addOnSuccessListener(authResult -> {
                                        Log.d("SignIn", "signInWithCredential:success");
                                        setResult(RESULT_OK);
                                        Log.d("Auth", "AuthActivity: Successfully logged in");
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("SignIn", "signInWithCredential:failed");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.d("SignIn", "signInWithGoogle:failed");
                            finish();
                        });
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        checkAuth();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .requestIdToken(getString(R.string.client_id))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.d("Auth", "AuthActivity: Launching activity to sign in with Google");
        launcher.launch(googleSignInClient.getSignInIntent());
    }

    private void checkAuth() {
        if (user != null) {
            Log.d("Auth", "AuthActivity: Already logged in");
            setResult(RESULT_OK);
            finish();
        }
    }
}