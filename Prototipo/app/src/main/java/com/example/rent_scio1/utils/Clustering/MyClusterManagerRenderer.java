package com.example.rent_scio1.utils.Clustering;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rent_scio1.utils.map.MyMapClient;
import com.example.rent_scio1.utils.map.MyMapTrader;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

// classe di utilità per costruire i cluster dei marker visualizzati sulle mappe di cliente e commerciante.

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final String TAG = "MyClusterManagerRenderer";
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final Context context;
    private final Class c;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap, ClusterManager<ClusterMarker> clusterManager, Class c) {

        super(context, googleMap, clusterManager);
        this.context = context;
        this.c = c;
        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        int padding = 5;
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);


    }


    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        imageView.setImageBitmap(item.getImage());
        if(item.getColor() != null)
            imageView.setBackgroundColor(item.getColor());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster){
        if(c.equals(MyMapTrader.class)){
            return false;
        }else{
            super.shouldRenderAsCluster(cluster);
            return MyMapClient.shouldCluster_zoom;
        }
    }

    public void setUpdateMarker(ClusterMarker clusterMarker) {
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setPosition(clusterMarker.getPosition());
        }
    }

    public void setUpdateInfoWindow(ClusterMarker clusterMarker){
        Marker marker = getMarker(clusterMarker);
        if (marker != null) {
            marker.setSnippet(clusterMarker.getSnippet());
            if(marker.isInfoWindowShown())
                marker.showInfoWindow();
        }
    }



}
