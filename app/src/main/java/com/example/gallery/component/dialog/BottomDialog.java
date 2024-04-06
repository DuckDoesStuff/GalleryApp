package com.example.gallery.component.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.example.gallery.utils.AlbumManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class BottomDialog extends BottomSheetDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText albumName = view.findViewById(R.id.set_album_name);


        Button cancelBtn = view.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(v -> dismiss());

        Button createAlbumBtn = view.findViewById(R.id.btn_create);
        createAlbumBtn.setOnClickListener(v -> {
            String albumNameStr = albumName.getText().toString();
            if(albumNameStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter album name", Toast.LENGTH_SHORT).show();
            }else {
                if (AlbumManager.createNewAlbum(requireContext(), albumNameStr) != null) {
                    Toast.makeText(getContext(), "Album created successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Album already exist", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}