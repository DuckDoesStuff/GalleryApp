package com.example.gallery.component;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.utils.database.AlbumModel;

import java.util.ArrayList;
import java.util.Objects;


public class AlbumFrameAdapter extends RecyclerView.Adapter<AlbumFrameAdapter.AlbumViewHolder> {
    private final AlbumFrameAdapter.AlbumFrameListener onClickCallBack;
    private ArrayList<AlbumFrameModel> frameModels;

    public AlbumFrameAdapter(AlbumFrameListener onClickCallBack, ArrayList<AlbumModel> albums) {
        this.onClickCallBack = onClickCallBack;
        initFrameModels(albums);
    }

    @NonNull
    @Override
    public AlbumFrameAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the RecyclerView item
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_frame, parent, false);

        // Create a new ViewHolder with the inflated layout
        AlbumViewHolder viewHolder = new AlbumViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumFrameAdapter.AlbumViewHolder holder, int position) {
        holder.bind(frameModels.get(position));
        holder.itemView.setOnClickListener(v -> {
            if (frameModels.get(position).album.mediaCount != 0)
                onClickCallBack.onItemClick(position);
            else
                Toast.makeText(v.getContext(), "This album is empty!", Toast.LENGTH_SHORT).show();

        });
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return frameModels.size();
    }

    public void initFrameModels(ArrayList<AlbumModel> albums) {
        if (albums == null || albums.isEmpty()) {
            frameModels = new ArrayList<>();
        } else {
            frameModels = new ArrayList<>();
            for (AlbumModel albumModel : albums) {
                frameModels.add(new AlbumFrameModel(albumModel));
            }
        }
        notifyDataSetChanged();
    }

    public interface AlbumFrameListener {
        default void onItemClick(int position) {

        }

        default void onItemLongClick(int position) {
        }
    }

    public static class AlbumFrameModel {
        public AlbumModel album;

        public AlbumFrameModel(AlbumModel albumModel) {
            album = albumModel;
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        AlbumFrameModel albumFrameModel;
        ImageView albumThumbnail;
        TextView albumName;
        TextView albumCount;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
            albumName = itemView.findViewById(R.id.album_name);
            albumCount = itemView.findViewById(R.id.album_count);
        }

        public void bind(AlbumFrameModel albumFrameModel) {
            this.albumFrameModel = albumFrameModel;

            if (Objects.equals(albumFrameModel.album.albumThumbnail, "")) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                albumThumbnail.setColorFilter(new ColorMatrixColorFilter(matrix));
            } else {
                Glide.with(itemView)
                        .load(albumFrameModel.album.albumThumbnail)
                        .centerCrop()
                        .into(albumThumbnail);
            }

            albumName.setText(albumFrameModel.album.albumName);
            albumCount.setText(String.valueOf(albumFrameModel.album.mediaCount));
        }
    }
}
