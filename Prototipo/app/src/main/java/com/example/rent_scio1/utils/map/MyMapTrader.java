package com.example.rent_scio1.utils.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MyMapTrader extends MyMap{

    private User mTrader;
    private static final String TAG = "MyMapTrader";

    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    //private GoogleMap mMap;
    private final HashMap<String, Marker> listMarker = new HashMap<>();

    private final Context context;

    private StorageReference mStorageRef;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        getUserDetails(googleMap);
        areaLimitata();
        searchCustomers();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        getmMap().setInfoWindowAdapter(new CustomInfoWindowAdapter(context, this.getClass()));

        getmMap().setOnMarkerClickListener(marker -> {

            marker.showInfoWindow();

            return true;
        });

    }


    public MyMapTrader(Context context) {
        this.context = context;
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

    private void addDocument(MarkerOptions markerOptions,DocumentChange dc){

        Run run=dc.getDocument().toObject(Run.class);

        if(run.getGeoPoint()!=null)
            markerOptions.position(new LatLng(run.getGeoPoint().getLatitude(),run.getGeoPoint().getLongitude()));
        else
            markerOptions.position(new LatLng(UserClient.getUser().getTraderPosition().getLatitude(),UserClient.getUser().getTraderPosition().getLongitude()));
        Marker costumer = getmMap().addMarker(markerOptions);


        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

        new CountDownTimer(time,1000){

            @Override
            public void onTick(long millisUntilFinished) {
                int minutes=(int) (millisUntilFinished / 1000) / 60;
                int seconds=(int) (millisUntilFinished / 1000) % 60;


                if(minutes>=60){
                    int hours=minutes/60;
                    minutes=minutes-(hours*60);

                    String hoursText=""+hours;
                    if(hours<10){

                        hoursText="0"+hoursText;
                    }

                    String minutesText=""+minutes;
                    if(minutes<10){

                        minutesText="0"+minutesText;
                    }

                    costumer.setSnippet( (run.getSpeed())+" "+hoursText+":"+minutesText+":"+seconds );
                }
                else{

                    String minutesText=""+minutes;
                    if(minutes<10){

                        minutesText="0"+minutesText;
                    }

                    costumer.setSnippet((run.getSpeed())+" "+minutesText+":"+seconds );
                }

                if(costumer.isInfoWindowShown())
                    costumer.showInfoWindow();
            }

            @Override
            public void onFinish() {
                costumer.setSnippet( run.getSpeed()+" "+"TERMINATO");

                if(costumer.isInfoWindowShown())
                    costumer.showInfoWindow();
            }
        }.start();

        listMarker.put(dc.getDocument().toObject(Run.class).getUser(),costumer);
    }

    private void searchCustomers() {
        FirebaseFirestore.getInstance().collection("run")
                .whereEqualTo("trader", UserClient.getUser().getUser_id())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        Log.e(TAG, "PRE SWITCH");
                        switch (dc.getType()) {
                            case ADDED:
                                Log.e(TAG, "ADDED");
                                Query getVehiclesTrader = FirebaseFirestore.getInstance().collection("users").whereEqualTo("user_id", dc.getDocument().toObject(Run.class).getUser());

                                getVehiclesTrader.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            User user = new User(document.toObject(User.class));

                                            StorageReference islandRef = mStorageRef.child("users/" + user.getUser_id() + "/avatar.jpg");
                                            Log.e(TAG, "COGNOME: "+user.getSurname());
                                            MarkerOptions markerOptions=new MarkerOptions().title(user.getName() + " " + user.getSurname());

                                            File localFile;

                                            try {
                                                localFile = File.createTempFile("images", "jpg");
                                                islandRef.getFile(localFile)
                                                        .addOnSuccessListener(taskSnapshot -> {

                                                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(localFile.getPath(),150,150)));
                                                            addDocument(markerOptions,dc);

                                                        })
                                                        .addOnFailureListener(exception -> {
                                                            Log.e(TAG, "NON caricata");
                                                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(  Bitmap.createScaledBitmap( getBitmap(R.drawable.logo_vettorizzato_transp),250,250,false) ));
                                                            addDocument(markerOptions,dc);
                                                        });


                                            } catch (IOException ioException) {
                                                Log.e(TAG, "Errore nel caricamento dell'immaigne");
                                                ioException.printStackTrace();
                                            }

                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                });
                                break;
                            case MODIFIED:
                                Log.e(TAG, "MODIFIED");
                                modifyMarker(dc.getDocument().toObject(Run.class));
                                break;
                            case REMOVED:
                                Log.e(TAG, "REMOVED");
                                clearMarker(dc.getDocument().toObject(Run.class));
                                break;
                        }
                    }
                });

    }

    private void modifyMarker(Run run){
        if(run.getGeoPoint() != null){
            Marker costumer=listMarker.get(run.getUser());

            if(costumer!=null){

                costumer.setPosition(new LatLng(run.getGeoPoint().getLatitude(), run.getGeoPoint().getLongitude()));
            }

        }
    }

    private void clearMarker(Run run){
        Marker m = listMarker.get(run.getUser());
        listMarker.remove(run.getUser());
        m.remove();
    }

    private void getUserDetails(GoogleMap googleMap){
        if(mTrader == null){
            mTrader = new User();
            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: successfully get teh user details");
                    User user = task.getResult().toObject(User.class);
                    mTrader=new User(user);
                    UserClient.setUser(user);
                    setCameraView(googleMap);
                }
            });
        }
    }

    private void setCameraView(GoogleMap googleMap){
        double bottomBoundary = mTrader.getTraderPosition().getLatitude() - .01;
        double leftBoundary = mTrader.getTraderPosition().getLongitude() - .01;
        double topBoundary = mTrader.getTraderPosition().getLatitude()  + .01;
        double rightBoundary = mTrader.getTraderPosition().getLongitude() + .01;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        getmMap().moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

        MarkerOptions markerOptions=new MarkerOptions()
                .position(new LatLng(mTrader.getTraderPosition().getLatitude(), mTrader.getTraderPosition().getLongitude()))
                .title(UserClient.getUser().getShopName());

        StorageReference islandRef = mStorageRef.child("users/" + UserClient.getUser().getUser_id() + "/avatar.jpg");
        File localFile;
        try {
            localFile = File.createTempFile("images", "jpg");
            islandRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(localFile.getPath(),150,150)));
                        googleMap.addMarker(markerOptions);

                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "NON caricata");
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(  Bitmap.createScaledBitmap( getBitmap(R.drawable.negozio_vettorizzato),150,150,false) ));
                        googleMap.addMarker(markerOptions);
                    });


        } catch (IOException ioException) {
            Log.e(TAG, "Errore nel caricamento dell'immaigne");
            ioException.printStackTrace();
        }
    }

    private void areaLimitata(){
        if(UserClient.getUser()!=null && UserClient.getUser().getDelimited_area()!=null){
            List<LatLng> latLngs = new ArrayList<>();

            List<GeoPoint> geoPoints = UserClient.getUser().getDelimited_area();
            for (GeoPoint a:geoPoints) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }

            PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
            Polygon polygon=getmMap().addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.rgb( 111,163,167));
        }
    }
}
