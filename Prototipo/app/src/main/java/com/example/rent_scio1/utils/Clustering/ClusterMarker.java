package com.example.rent_scio1.utils.Clustering;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.Nullable;

import com.example.rent_scio1.Client.CustomInfoWindowAdapterClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.jetbrains.annotations.NotNull;

public class ClusterMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private Bitmap image;
    private String snippet;
    private String runId;
    private Integer color;

    public ClusterMarker(double lat, double lng, String title, Bitmap image, String runId, Integer color) {
        this.image = image;
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = "";
        this.runId = runId;
        this.color = color;
    }

    @NotNull
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
        return this.snippet;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }
}
