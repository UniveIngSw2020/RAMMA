package com.example.rent_scio1.utils.Clustering;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.rent_scio1.utils.map.MyMapClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MyClusterManagerRenderer extends DefaultClusterRenderer<ClusterMarker> {

    private final IconGenerator iconGenerator;
    private final ImageView imageView;

    public MyClusterManagerRenderer(Context context, GoogleMap googleMap, ClusterManager<ClusterMarker> clusterManager) {

        super(context, googleMap, clusterManager);
        iconGenerator = new IconGenerator(context.getApplicationContext());
        imageView = new ImageView(context.getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
        int padding = 5;
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);


    }

    /**
     * Rendering of the individual ClusterItems
     * @param item
     * @param markerOptions
     */



    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        imageView.setImageBitmap(item.getImage());
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).title(item.getTitle());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarker> cluster){
        super.shouldRenderAsCluster(cluster);
        return MyMapClient.shouldCluster_zoom;
    }


}
