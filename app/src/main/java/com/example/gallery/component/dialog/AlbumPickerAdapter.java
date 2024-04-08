package com.example.gallery.component.dialog;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.utils.GalleryDB;

import java.util.ArrayList;

public class AlbumPickerAdapter extends RecyclerView.Adapter<AlbumPickerAdapter.PickerViewHolder> {
    private final AlbumPickerListener listener;

    public interface AlbumPickerListener {
        void onAlbumSelected(GalleryDB.AlbumScheme albumScheme);
    }
    ArrayList<GalleryDB.AlbumScheme> albums;
    public AlbumPickerAdapter(ArrayList<GalleryDB.AlbumScheme> albums, AlbumPickerListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);

        return new PickerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PickerViewHolder holder, int position) {
        GalleryDB.AlbumScheme album = albums.get(position);
        ImageView albumCover = holder.itemView.findViewById(R.id.album_cover);
        TextView albumTitle = holder.itemView.findViewById(R.id.album_title);
        TextView albumCount = holder.itemView.findViewById(R.id.album_count);

        if (album.albumThumb != null)
            Glide.with(holder.itemView).load(album.albumThumb).centerCrop().into(albumCover);
        else {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            albumCover.setColorFilter(new ColorMatrixColorFilter(matrix));
        }
        albumTitle.setText(album.albumName);

        if(album.albumCount != 0)
            albumCount.setText(String.valueOf(album.albumCount));

        holder.itemView.setOnClickListener(v -> {
            listener.onAlbumSelected(album);
        });
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class PickerViewHolder extends RecyclerView.ViewHolder {
        public PickerViewHolder(android.view.View itemView) {
            super(itemView);
        }
    }
}
