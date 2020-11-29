package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.utils.PermissionUtils;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MyMap implements OnMapReadyCallback{

    private static GoogleMap mMap = null;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Nullable
    public static GoogleMap getmMap() {
        return mMap;
    }




    /*private void setCameraView(GoogleMap googleMap){
        User mTrader = UserClient.getUser();
        double bottomBundary = mTrader.getTraderposition().getLatitude() - .01;
        double leftBoundary = mTrader.getTraderposition().getLongitude() - .01;
        double topBoundary = mTrader.getTraderposition().getLatitude() + .01;
        double rightBoundary = mTrader.getTraderposition().getLongitude() + .01;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

        googleMap.addMarker(new MarkerOptions()
                .position( new LatLng(mTrader.getTraderposition().getLatitude(), mTrader.getTraderposition().getLongitude()))
                .title("Tu sei qui!"));
    }*/





    //stampiamo i poligoni
    /*private void setLimitedArea(ArrayList<List<GeoPoint>> pointlist){
        for (List<GeoPoint> l : pointlist){
            List<LatLng> latLngs = new ArrayList<>();
            for (GeoPoint a: l) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }
            PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
            Polygon polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.BLACK);
        }
    }*/
}
