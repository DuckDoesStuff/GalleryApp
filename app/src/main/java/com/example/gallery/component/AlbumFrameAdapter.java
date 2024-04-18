package com.example.gallery.component;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gallery.R;
import com.example.gallery.utils.database.AlbumModel;

import java.util.ArrayList;
import java.util.Objects;


public class AlbumFrameAdapter extends RecyclerView.Adapter<AlbumFrameAdapter.AlbumViewHolder> {
    private final AlbumFrameAdapter.AlbumFrameListener onClickCallBack;
    private final AlbumViewModel albumViewModel;

    public AlbumFrameAdapter(Fragment fragment, AlbumFrameListener onClickCallBack) {
        this.onClickCallBack = onClickCallBack;

        albumViewModel = new ViewModelProvider(fragment).get(AlbumViewModel.class);
        // Do things
        Observer<ArrayList<AlbumModel>> albumObserver = albums -> {
            // Do things
            notifyItemRangeChanged(0, albums.size());
            Log.d("AlbumFrameAdapter", "Album observer called with " + albums.size() + " items");
        };
        albumViewModel.getAlbums().observe(fragment.getViewLifecycleOwner(), albumObserver);

        // Do things
        Observer<ArrayList<AlbumModel>> selectedAlbumObserver = selectedAlbums -> {
            // Do things
            Log.d("AlbumFrameAdapter", "Selected album observer called with " + selectedAlbums.size() + " items");
        };
        albumViewModel.getSelectedAlbums().observe(fragment.getViewLifecycleOwner(), selectedAlbumObserver);
    }

    @NonNull
    @Override
    public AlbumFrameAdapter.AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_frame, parent, false);

        return new AlbumViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumFrameAdapter.AlbumViewHolder holder, int position) {
        AlbumModel albumModel = albumViewModel.getAlbum(position);
        holder.bind(albumModel);
        holder.itemView.setOnClickListener(v -> {
            if (albumModel.mediaCount != 0)
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
        return Objects.requireNonNull(albumViewModel.getAlbums().getValue()).size();
    }

    public interface AlbumFrameListener {
        default void onItemClick(int position) {

        }

        default void onItemLongClick(int position) {
        }
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        AlbumModel albumModel;
        ImageView albumThumbnail;
        TextView albumName;
        TextView albumCount;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumThumbnail = itemView.findViewById(R.id.album_thumbnail);
            albumName = itemView.findViewById(R.id.album_name);
            albumCount = itemView.findViewById(R.id.album_count);
        }

        public void bind(AlbumModel albumModel) {
            this.albumModel = albumModel;
            if (Objects.equals(albumModel.albumThumbnail, "")) {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                albumThumbnail.setColorFilter(new ColorMatrixColorFilter(matrix));
            } else {
                albumThumbnail.clearColorFilter();
                Glide.with(itemView)
                        .load(albumModel.albumThumbnail)
                        .centerCrop()
                        .into(albumThumbnail);
            }

            albumName.setText(albumModel.albumName);
            albumCount.setText(String.valueOf(albumModel.mediaCount));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d("AlbumFrameAdapter", "Finalized ImageFrameAdapter");
    }
}
