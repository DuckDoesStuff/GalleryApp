package com.example.gallery.component;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.gallery.R;

public class detailSheet extends BottomSheetDialogFragment {
    private String albumName;
    private String type;
    private String localPath;
    private String cloudPath;
    private String dateTaken;

    // Có thể bạn sẽ nhận được dữ liệu thông qua các phương thức như setter hoặc bundle.
    public void setDetailData(String albumName, String type, String localPath, String cloudPath, String dateTaken) {
        this.albumName = albumName;
        this.type = type;
        this.localPath = localPath;
        this.cloudPath = cloudPath;
        this.dateTaken = dateTaken;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail_sheet, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tìm các TextView
        TextView albumText = view.findViewById(R.id.album_text);
        TextView typeText = view.findViewById(R.id.type_text);
        TextView localPathText = view.findViewById(R.id.local_path_text);
        TextView cloudPathText = view.findViewById(R.id.cloud_path_text);
        TextView dateTakenText = view.findViewById(R.id.date_taken_text);

        // Đặt nội dung cho TextView
        albumText.setText(albumName);
        typeText.setText(type);
        localPathText.setText(localPath);
        cloudPathText.setText(cloudPath);
        dateTakenText.setText(dateTaken);
    }
}
