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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class BottomDialogAddAlbum extends BottomSheetDialogFragment {
    public static interface OnAddDialogListener {
        void onAlbumCreated(String albumName);
    }
    private OnAddDialogListener listener;
    public BottomDialogAddAlbum(OnAddDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_dialog_add_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCancelable(false);
        TextInputEditText albumName = view.findViewById(R.id.set_album_name);


        Button cancelBtn = view.findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(v -> {
            listener.onAlbumCreated(null);
            dismiss();
        });

        Button createAlbumBtn = view.findViewById(R.id.btn_create);
        createAlbumBtn.setOnClickListener(v -> {
            String albumNameStr = albumName.getText().toString();
            if (albumNameStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter album name", Toast.LENGTH_SHORT).show();
            } else {
                listener.onAlbumCreated(albumNameStr);
                dismiss();
            }
        });

    }
}