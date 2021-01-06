package com.example.rent_scio1.utils.Clustering;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;

import com.example.rent_scio1.Client.CustomInfoWindowAdapterClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private Bitmap image;

    public ClusterMarker(double lat, double lng, String title, Bitmap image) {
        this.image = image;
        position = new LatLng(lat, lng);
        this.title = title;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }

    public Bitmap getImage() {
        return image;
    }
}