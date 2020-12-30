package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Pair;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.Vehicle;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyMapClient extends MyMap {

    private static final String TAG = "MyMapClient";

    private final AppCompatActivity context;

    private ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader = new ArrayList<>();

    private final FusedLocationProviderClient mFusedLocationClient;

    private boolean mLocationPermissionGranted = false;

    LocationManager manager;

    DialogInterface.OnClickListener listener;

    private StorageReference mStorageRef;

    public MyMapClient(AppCompatActivity context, LocationManager manager, DialogInterface.OnClickListener listener, ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader, Vehicle vehicleRun) {
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
        MyPermission permission = new MyPermission(context, context, location -> {
        });

        boolean bol = permission.checkMapServices(
                "L'applicazione per settare la posizione del negozio in automatico ha bisogno che la geolocalizzazione sia attiva dalle impostazioni.",
                "OK", manager, listener);

        if (bol) {

            if (!mLocationPermissionGranted) {

                mLocationPermissionGranted = permission.getLocationPermission(
                        "L'applicazione per settare la posizione del negozio in automatico ha bisogno del permesso della posizione.",
                        "Hai rifiutato il permesso :( , dovrai settare la posizione manualmente o attivare il permesso dalle impostazioni di sistema",
                        "Ok", "Voglio proseguire senza permessi", (dialog, which) ->
                                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MyPermission.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

            }
        }

        location();

        setMarkerDelimitedTraderNotify();

        getmMap().setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            Log.e(TAG, "MARKER CLICCATOOOOOOOOOOOOO");


            for(Pair<User, Pair<Float, Polygon>> trader: listTrader) {

                if (trader.getFirst().getDelimited_area() != null) {

                    if (marker.getPosition().equals(new LatLng(trader.getFirst().getTraderPosition().getLatitude(), trader.getFirst().getTraderPosition().getLongitude()))) {
                        int col = Color.HSVToColor(new float[]{trader.getSecond().getFirst(), 0.2f, 1.0f});
                        trader.getSecond().getSecond().setFillColor(Color.argb(130, Color.red(col), Color.green(col), Color.blue(col)));
                        trader.getSecond().getSecond().setVisible(!trader.getSecond().getSecond().isVisible());

                    }
                    else {

                        trader.getSecond().getSecond().setVisible(false);
                        trader.getSecond().getSecond().setFillColor(android.R.color.transparent);
                    }

                }
            }
            return true;
        });

        getmMap().setOnMapClickListener(latLng -> {
            for(Pair<User, Pair<Float, Polygon>> trader : listTrader){
                if(trader.getFirst().getDelimited_area() != null ){
                    trader.getSecond().getSecond().setVisible(false);
                    trader.getSecond().getSecond().setFillColor(android.R.color.transparent);
                }
            }
        });
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

    private void setMarkerDelimitedTraderNotify(){

        Log.e(TAG, "setMarkerDelimitedTraderNotify " + listTrader.size());

        for (Pair<User, Pair<Float, Polygon>> trader : listTrader) {

            Log.e(TAG, "                    " + trader.getFirst().toString());

            if (trader.getFirst().getTraderPosition() != null) {


                MarkerOptions markerOptionsTrader= new MarkerOptions()
                        .position(new LatLng(trader.getFirst().getTraderPosition().getLatitude(), trader.getFirst().getTraderPosition().getLongitude()))
                        .title(trader.getFirst().getShopName())
                        .snippet("Negozio di: " + trader.getFirst().getSurname() + " " + trader.getFirst().getName());

                try {

                    StorageReference islandRef = mStorageRef.child("users/" + trader.getFirst().getUser_id() + "/avatar.jpg");
                    File localFile = File.createTempFile( trader.getFirst().getUser_id() , "jpg");

                    islandRef.getFile(localFile)
                            .addOnSuccessListener(taskSnapshot ->{
                                        //icona personalizzata
                                        markerOptionsTrader.icon( BitmapDescriptorFactory.fromBitmap( resizeMapIcons(localFile.getPath(),100,100)) );
                                        getmMap().addMarker(markerOptionsTrader);
                                    })
                            .addOnFailureListener( exception ->{
                                        //icona di default
                                        markerOptionsTrader.icon( BitmapDescriptorFactory.fromBitmap(  Bitmap.createScaledBitmap(getBitmap(R.drawable.negozio_vettorizzato),100,100,false) ));
                                        getmMap().addMarker(markerOptionsTrader);
                                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            List<LatLng> latLngs = new ArrayList<>();
            if(trader.getFirst().getDelimited_area() != null){
                for (GeoPoint a: trader.getFirst().getDelimited_area()) {
                    latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
                }

                PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
                Polygon polygon=getmMap().addPolygon(polygonOptions);
                polygon.setVisible(false);
                float [] col = new float[] { trader.getSecond().getFirst(), 1.0f, 1.0f };
                Log.e(TAG, trader.getSecond().getFirst() + "         " + col[0]);
                polygon.setStrokeColor(Color.HSVToColor(col));
                polygon.setStrokeWidth(5.0f);
                trader.getSecond().setSecond(polygon);
            }

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
        }).addOnSuccessListener(this::setCameraView);
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
