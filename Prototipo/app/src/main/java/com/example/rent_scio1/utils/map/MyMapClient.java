package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.MyNotify;
import com.example.rent_scio1.utils.Pair;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MyMapClient extends MyMap {

    //private ArrayList<User> listTrader = new ArrayList<>();



    private static final String TAG = "MyMapClient";

    private AppCompatActivity context;

    private MyNotify mNotifyDelimitedArea;
    private MyNotify mNotifySpeed;

    private ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader = new ArrayList<>();
    private Vehicle vehicleRun;
    private Polygon fillPol = null;

    private FusedLocationProviderClient mFusedLocationClient;
    private static CountDownTimer timerDelimitedArea;

    private boolean mLocationPermissionGranted = false;

    LocationManager manager;

    DialogInterface.OnClickListener listener;

    private StorageReference mStorageRef;

    public MyMapClient() {
        super();
    }

    public MyMapClient(AppCompatActivity context, LocationManager manager, DialogInterface.OnClickListener listener, ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader, Vehicle vehicleRun) {
        super();
        this.context = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.manager = manager;
        this.listener = listener;
        this.listTrader = listTrader;
        this.vehicleRun = vehicleRun;
    }

    /*
    1- in commerciante solo la sua
    2- in clinete senza corsda tutte
    3- in clinete con corsa attiva solo quella affine
    */

    public void location(){
        if (getmMap() != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getmMap().setMyLocationEnabled(true);
            getLastKnownLocation();
            //getUserDetails();
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
        //setMapDetails();
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
            /*if(fillPol != null) {
                Log.e(TAG, "TRASPARENTEEEEEE");
                fillPol.setVisible(false);
                fillPol.setFillColor(android.R.color.transparent);
                fillPol = null;
            }*/
        });
    }



    /*private void setMapDetails(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query getTrader;

        if(UserClient.getRun() != null){

            getTrader = db.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        }else{
            getTrader = db.collection("users").whereEqualTo("trader", true);
        }


        getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                User u=new User(document.toObject(User.class));
                if(u.getTraderposition()!=null)
                    listTrader.add(u);
            }

            if(UserClient.getRun() != null){
                Query getVehicle= db.collection("vehicles").whereEqualTo("user_id", UserClient.getRun().getVehicle());

                getVehicle.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        vehicleRun=new Vehicle(document.toObject(Vehicle.class));
                    }

                    setMarkerDelimitedTraderNotify();
                });
            }
            else{
                setMarkerDelimitedTraderNotify();
            }

        });
    }*/

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
                                        markerOptionsTrader.icon( BitmapDescriptorFactory.fromBitmap( resizeMapIcons(localFile.getPath(),100,100)) );
                                        getmMap().addMarker(markerOptionsTrader);
                                    })
                            .addOnFailureListener( exception ->{
                                        markerOptionsTrader.icon( BitmapDescriptorFactory.fromBitmap(  Bitmap.createScaledBitmap(getBitmap(R.drawable.negozio_vettorizzato),100,100,false) ));
                                        getmMap().addMarker(markerOptionsTrader);
                                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            List<LatLng> latLngs = new ArrayList<>();
            if(trader.getFirst().getDelimited_area() != null){
                //Log.e(TAG, trader.getDelimited_area().toString());
                for (GeoPoint a: trader.getFirst().getDelimited_area()) {
                    //Log.e(TAG,"Sto creando la lista di latlang");
                    latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
                }
                //Log.e(TAG,"stampo il poligono");


                PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
                Polygon polygon=getmMap().addPolygon(polygonOptions);
                polygon.setVisible(false);
                float [] col = new float[] { trader.getSecond().getFirst(), 1.0f, 1.0f };
                Log.e(TAG, trader.getSecond().getFirst() + "         " + col[0]);
                polygon.setStrokeColor(Color.HSVToColor(col));
                polygon.setStrokeWidth(5.0f);
                //listTraderPolygon.add(new Pair<>(trader.first, polygon));
                trader.getSecond().setSecond(polygon);
            }


            if(UserClient.getRun()!=null){
                mNotifyDelimitedArea = new MyNotify(context, "delimitedAreaChannel", "Uscita dall'area limitata", "Avvisa l'utente dell'uscita dall'area limitata","Attenzione!","Hai oltrepassato l'area limitata!", R.drawable.ic_not_permitted);
                mNotifySpeed = new MyNotify(context, "speedChannel", "Velocità elevata", "Avvisa l'utente della velocità troppo elevata","Attenzione!","Stai andando troppo veloce!", R.drawable.ic_not_permitted);
                //createNotification(UserClient.getRun(), mNotifyDelimitedArea.getNotify(),mNotifySpeed.getNotify());
                //timerDelimitedArea.start();
            }

        }
    }

/*
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (getmMap() != null) {
                getmMap().setMyLocationEnabled(true);
                getLastKnownLocation();
                //getUserDetails();
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission

            PermissionUtils.requestPermission(context, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }*/


    private void createNotification(Run run, Notification dilimitedAreaNotification, Notification speedNotification ) {

        int delimitedAreaNotificationID=0;
        int speedNotificationID=1;

        // calcolo il tempo rimanente alla fine della corsa, in questo modo non spreco risorse.
        // nel caso peggiore il cliente non uscirà mai da questa schermata e dovrò aggiornare ogni minuto della corsa.
        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

//        timerDelimitedArea = new CountDownTimer(time,10000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//                if(run != null && run.getGeoPoint() != null && listTrader.get(0).getFirst().getDelimited_area() != null){
//
//                    LatLng position=new LatLng(run.getGeoPoint().getLatitude(),run.getGeoPoint().getLongitude());
//
//                    List<LatLng> latLngs = new ArrayList<>();
//                    for (GeoPoint a: listTrader.get(0).getFirst().getDelimited_area()) {
//                        latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
//                    }
//
//                    Log.e(TAG,"ENTRATO QUA: pre notifica");
//
//                    if(!PolyUtil.containsLocation(position, latLngs,true)){
//
//                        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//                        // notificationId is a unique int for each notification that you must define
//                        //MyNotify notifyTrader = new MyNotify(context, "warning", "Ciao fra", "prova", "Ciao fra", "stustustustu", R.drawable.ic_not_permitted);
//
//                        //mNotifyDelimitedArea.getNotificationManager().notify(delimitedAreaNotificationID, dilimitedAreaNotification);
//                    }
//
//                    //notifica di velocità
//                    //bisogna fare una query per prendere la velocità del veicolo
//                    if(run.getSpeed()>vehicleRun.getMaxSpeedKMH()){
//                        Log.e(TAG,"ENTRATO QUA: notifica velocità");
//                        //mNotifySpeed.getNotificationManager().notify(speedNotificationID,speedNotification);
//                    }
//                }
//
//                Log.e(TAG,"TICK TIMER");
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        };

    }

    public static void stopNotification() {
        Log.e(TAG, "STOP DELLA NOTIFICA");
        if(timerDelimitedArea != null){
            timerDelimitedArea.cancel();
        }
    }



    /*private void delimitedArea(Run r) {

        User u = UserClient.getUser();

        //se la corsa non è più attiva durante il countdown interrompo l'aggiornamento.
        Run run = UserClient.getRun();
        if (run != null) {
            //query per trovarare il commerciante associato
            Query getTrader = FirebaseFirestore.getInstance().collection("users").whereEqualTo("user_id", r.getTrader());
            getTrader.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        //una volta trovato il commerciante mi getto la sua delimited area
                        User trader = new User(document.toObject(User.class));
                        List<GeoPoint> geoPoints = trader.getDelimited_area();

                        //se il commerciante ha impostato l'area limitata vado
                        if (geoPoints != null) {


                            //setto la delimited area nell'oggeto user di cliente
                            u.setDelimited_area(geoPoints);

                        }
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            });
        }
    }*/


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


    /*@Override
    public boolean onMarkerClick(Marker marker) {
        Log.e(TAG, "MARKER CLICCATOOOOOOOOOOOOO");
        for(Pair<User, Pair<Float, Polygon>> trader: listTrader){
            if(marker.getPosition().equals(trader.getFirst().getTraderposition())){
                trader.getSecond().getSecond().setFillColor(Color.HSVToColor(new float[] { trader.getSecond().getFirst(), 0.3f, 1.0f }));
            }
        }
        return true;
    }*/
}
