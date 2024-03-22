package com.example.gallery.component;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gallery.R;

import java.util.ArrayList;



public class AlbumFrameAdapter extends RecyclerView.Adapter<AlbumFrameAdapter.AlbumViewHolder> {
    public static class AlbumModel {
        String albumName;
        int numOfImg;
        String thumbnail;
        public AlbumModel (String name, int n, String thumb) {
            this.albumName = name;
            this.numOfImg = n;
            this.thumbnail = thumb;
        }
    }
    private ArrayList<AlbumModel> albums;

    static class AlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView albumThumbnail;
        TextView albumName;
        TextView albumCount;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
            albumName = itemView.findViewById(R.id.album_name);
            albumCount = itemView.findViewById(R.id.album_count);
        }
    }


    public AlbumFrameAdapter() {
        super();
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
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public AlbumFrameAdapter(ArrayList<AlbumModel> albums) {
        this.albums = albums;
    }
}
