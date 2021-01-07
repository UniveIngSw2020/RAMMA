package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.Client.CustomInfoWindowAdapterClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Clustering.ClusterMarker;
import com.example.rent_scio1.utils.Clustering.MyClusterManagerRenderer;
import com.example.rent_scio1.utils.Pair;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyMapClient extends MyMap {

    private static final String TAG = "MyMapClient";
    private final AppCompatActivity context;
    private final ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader;
    private final FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private Location actualLocation;
    private LatLng posMarker = null;
    private ClusterManager<ClusterMarker> clusterManager = null;
    private MyClusterManagerRenderer mClusterManagerRenderer = null;
    public static boolean shouldCluster_zoom;

    LocationManager manager;

    DialogInterface.OnClickListener listener;

    private StorageReference mStorageRef;

    public MyMapClient(AppCompatActivity context, LocationManager manager, DialogInterface.OnClickListener listener, ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader) {
        super();
        this.context = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.manager = manager;
        this.listener = listener;
        this.listTrader = listTrader;
    }



    public void location(){
        if (getmMap() != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getmMap().setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        //enableMyLocation();
        Log.e(TAG, "MAPPA PRONTA");

        mStorageRef = FirebaseStorage.getInstance().getReference();

        //passo una lambda nulla
        MyPermission permission = new MyPermission(context, context, location -> { });

        boolean bol = permission.checkMapServices(
                "L'applicazione per funzionare correttamente ha bisogno che la geolocalizzazione sia attiva dalle impostazioni.",
                "OK", manager, listener);

        if (bol) {

            if (!mLocationPermissionGranted) {

                mLocationPermissionGranted = permission.getLocationPermission(
                        "L'applicazione per funzionare correttamente ha bisogno del permesso della posizione.",
                        "Hai rifiutato il permesso :( , se vuoi utilizzare l'app dovrai consentire l'accesso alla posizione da impostazioni.",
                        "Ok", "Voglio proseguire senza permessi", (dialog, which) ->
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

            }
        }

        location();

        clusterManager = new ClusterManager<>(context, getmMap());
        if(mClusterManagerRenderer == null){
            mClusterManagerRenderer = new MyClusterManagerRenderer(context, getmMap(), clusterManager);
            clusterManager.setRenderer(mClusterManagerRenderer);
        }

        getmMap().setOnCameraIdleListener(() -> {
            shouldCluster_zoom = getmMap().getCameraPosition().zoom < 12;
            Log.e(TAG, "ZOOM: " + getmMap().getCameraPosition().zoom);
            clusterManager.onCameraIdle();
        });

        setMarkerDelimitedTrader();

        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapterClient(context));
        getmMap().setInfoWindowAdapter(clusterManager.getMarkerManager());
        //getmMap().setOnCameraIdleListener(clusterManager);



        clusterManager.setOnClusterItemClickListener(item -> {
            for(Pair<User, Pair<Float, Polygon>> trader: listTrader) {
                if (item.getPosition().equals(new LatLng(trader.getFirst().getTraderPosition().getLatitude(), trader.getFirst().getTraderPosition().getLongitude()))) {
                    if (trader.getFirst().getDelimited_area() != null) {
                        LatLng pos = item.getPosition();
                        if(trader.getSecond().getSecond().isVisible()){

                            if(posMarker == null || !posMarker.equals(pos)){
                                Log.e(TAG, "Rimostro l'info window");

                                for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                                    if(m.getPosition().equals(item.getPosition())){
                                        m.showInfoWindow();
                                        Log.e(TAG, "Mostro l'area limitata");
                                    }
                                }
                                posMarker = pos;

                            }else{

                                for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                                    if(m.getPosition().equals(item.getPosition())){
                                        m.hideInfoWindow();
                                        Log.e(TAG, "Cancello l'aree limitata");
                                    }
                                }
                                posMarker = null;
                                trader.getSecond().getSecond().setVisible(false);
                                trader.getSecond().getSecond().setFillColor(android.R.color.transparent);
                            }
                        }else{
                            for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                                if(m.getPosition().equals(item.getPosition())){
                                    m.showInfoWindow();
                                    Log.e(TAG, "Mostro l'area limitata con area limitata");
                                }
                            }

                            posMarker = pos;
                            int col = Color.HSVToColor(new float[]{trader.getSecond().getFirst(), 0.2f, 1.0f});
                            trader.getSecond().getSecond().setFillColor(Color.argb(130, Color.red(col), Color.green(col), Color.blue(col)));
                            trader.getSecond().getSecond().setVisible(!trader.getSecond().getSecond().isVisible());
                        }

                    }else{
                        Toast.makeText(context, "Il negozio " + trader.getFirst().getShopName() + " non ha un area limitata", Toast.LENGTH_LONG).show();
                        for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                            if(m.getPosition().equals(item.getPosition())){
                                m.showInfoWindow();
                                Log.e(TAG, "Mostro linfowindow senza arealimitata");
                            }
                        }
                    }
                }
            }
            return true;
        });

        getmMap().setOnMarkerClickListener(clusterManager);

        getmMap().setOnMapClickListener(latLng -> {
            for(Pair<User, Pair<Float, Polygon>> trader : listTrader){
                if(trader.getFirst().getDelimited_area() != null ){
                    Log.e(TAG, "Cancello tutte le aree limitate");
                    trader.getSecond().getSecond().setVisible(false);
                    trader.getSecond().getSecond().setFillColor(android.R.color.transparent);
                }
            }
        });

        clusterManager.setOnClusterItemInfoWindowClickListener(
                item -> {
                    Log.e(TAG,"Click infowindow");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setMessage("Vuoi attivare la navigazione?")
                            .setCancelable(true)
                            .setPositiveButton( "Sì", (dialogInterface, i) -> {
                                Log.e(TAG,"SI creo la navigazione");

                                String uri = "http://maps.google.com/maps?saddr=" + actualLocation.getLatitude() + "," + actualLocation.getLongitude() + "&daddr=" + item.getPosition().latitude + "," + item.getPosition().longitude;
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                context.startActivity(intent);

                                dialogInterface.dismiss();
                            }).setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                    final AlertDialog alert = builder.create();
                    alert.show();
                });

        getmMap().setOnInfoWindowClickListener(clusterManager);

    }



    public Bitmap resizeMapIcons(String filePath, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public boolean isMarkerPresent(LatLng pos){
        boolean flag = false;
        for(Marker m : clusterManager.getClusterMarkerCollection().getMarkers()){
            if(m.getPosition().equals(pos))
                flag = true;
        }
        return flag;
    }



    private void setMarkerDelimitedTrader(){
        if(clusterManager.getMarkerCollection().getMarkers().size() == 0) {
            for (Pair<User, Pair<Float, Polygon>> trader : listTrader) {
                Log.e(TAG, "                    " + trader.getFirst().toString());
                if (trader.getFirst().getTraderPosition() != null) {
                    GeoPoint pos = trader.getFirst().getTraderPosition();
                    String title = trader.getFirst().getShopName();
                    if (!isMarkerPresent(new LatLng(pos.getLatitude(), pos.getLongitude()))) {
                        try {
                            StorageReference islandRef = mStorageRef.child("users/" + trader.getFirst().getUser_id() + "/avatar.jpg");
                            File localFile = File.createTempFile(trader.getFirst().getUser_id(), "jpg");

                            islandRef.getFile(localFile)
                                    .addOnCompleteListener(task -> {
                                        Bitmap image;
                                        if (task.isSuccessful()) {
                                            image = resizeMapIcons(localFile.getPath(), 100, 100);
                                        } else {
                                            image = Bitmap.createScaledBitmap(getBitmap(R.drawable.negozio_vettorizzato), 100, 100, false);
                                        }
                                        Log.e(TAG, "                                                                                    aggiunto marker al cluster");
                                        clusterManager.addItem(new ClusterMarker(pos.getLatitude(), pos.getLongitude(), title, image));
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                List<LatLng> latLngs = new ArrayList<>();
                if (trader.getFirst().getDelimited_area() != null) {
                    for (GeoPoint a : trader.getFirst().getDelimited_area()) {
                        latLngs.add(new LatLng(a.getLatitude(), a.getLongitude()));
                    }

                    PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngs).clickable(false);
                    Polygon polygon = getmMap().addPolygon(polygonOptions);
                    polygon.setVisible(false);
                    float[] col = new float[]{trader.getSecond().getFirst(), 1.0f, 1.0f};
                    Log.e(TAG, trader.getSecond().getFirst() + "         " + col[0]);
                    polygon.setStrokeColor(Color.HSVToColor(col));
                    polygon.setStrokeWidth(5.0f);
                    trader.getSecond().setSecond(polygon);
                }
            }
            clusterManager.cluster();
        }
    }


    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation: called.");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //TODO MOVE CAMERA AUTOMATIc
        mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return this;
            }
        }).addOnSuccessListener(location -> setCameraView(actualLocation = location));
    }

    private void setCameraView(Location location) {
        try {

            double bottomBundary = location.getLatitude() - .01;
            double leftBoundary = location.getLongitude() - .01;
            double topBoundary = location.getLatitude() + .01;
            double rightBoundary = location.getLongitude() + .01;

            LatLngBounds mMapBoundary = new LatLngBounds(
                    new LatLng(bottomBundary, leftBoundary),
                    new LatLng(topBoundary, rightBoundary)
            );

            getmMap().moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        } catch (Exception e) {
        }
    }


}
