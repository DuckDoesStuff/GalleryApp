package com.example.gallery.component;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class AlbumFrameAdapter extends BaseAdapter {
    private class AlbumModel {
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

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public AlbumModel getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }


    public AlbumFrameAdapter (ArrayList<AlbumModel> albums) {
        this.albums = albums;
    }
}
