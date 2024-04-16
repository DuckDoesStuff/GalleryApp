package com.example.gallery.component.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.utils.MediaModel;

import java.util.List;

public class MediaSearchAdapter extends RecyclerView.Adapter<MediaSearchAdapter.MediaViewHolder> {
    private List<MediaModel> mediaList;

    public MediaSearchAdapter(List<MediaModel> mediaList) {
        this.mediaList = mediaList;
    }

    public  void setMediaList(List<MediaModel> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.search_item, parent, false);
        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaModel media = mediaList.get(position);
        holder.bind(media);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class MediaViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView nameTextView;
        private TextView pathTextView;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
        }

        public void bind(MediaModel media) {
            // Hiển thị hình ảnh
            // Đảm bảo rằng bạn có thư viện Picasso hoặc Glide để tải hình ảnh từ URL hoặc đường dẫn
            Glide.with(itemView.getContext()).load(media.path).into(imageView);

            // Hiển thị tên và đường dẫn
            nameTextView.setText(media.name);
            pathTextView.setText(media.path);
        }
    }
}