package com.example.gallery.component.dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gallery.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomDialogDuplicateItem extends BottomSheetDialogFragment {
    public interface OnDuplicateItemListener {
        void onApplyToAll(boolean applyToAll);
        void onSkip(boolean skip);
        void onReplace(boolean replace);
        void onRename(boolean rename);
        void onDismiss();
    }
    private String fileName;

    private OnDuplicateItemListener listener;

    public void setOnDuplicateItemListener(OnDuplicateItemListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            dismiss();
        }

        fileName = intent.getStringExtra("fileName");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_dialog_duplicate_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textView = view.findViewById(R.id.duplicate_item_text);
        textView.setText(fileName);

        // Handle click events
        CheckBox checkBox = view.findViewById(R.id.apply_to_all);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onApplyToAll(isChecked);
            }
        });

        view.findViewById(R.id.btn_skip).setOnClickListener(v -> {
            // Handle Skip
            if (listener != null) {
                listener.onSkip(true);
            }
            dismiss();
        });

        view.findViewById(R.id.btn_replace).setOnClickListener(v -> {
            // Handle Replace
            if (listener != null) {
                listener.onReplace(true);
            }
            dismiss();
        });

//        view.findViewById(R.id.btn_rename).setOnClickListener(v -> {
//            // Handle Rename
//            if (listener != null) {
//                listener.onRename(true);
//            }
//            dismiss();
//        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onDismiss();
        }
    }
}
