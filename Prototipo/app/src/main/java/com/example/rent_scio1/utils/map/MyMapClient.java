package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.MyNotify;
import com.example.rent_scio1.utils.Pair;
import com.example.rent_scio1.utils.permissions.MyPermission;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.Vehicle;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
            for(Pair<User, Pair<Float, Polygon>> trader: listTrader){
                if(trader.getSecond().getSecond() != null && marker.getPosition().equals(new LatLng(trader.getFirst().getTraderposition().getLatitude(), trader.getFirst().getTraderposition().getLongitude()))){
                    trader.getSecond().getSecond().setFillColor(Color.HSVToColor(new float[] { trader.getSecond().getFirst(), 0.2f, 1.0f }));
                    fillPol = trader.getSecond().getSecond();
                    return true;
                }
            }
            return false;
        });

        getmMap().setOnMapClickListener(latLng -> {
            if(fillPol != null) {
                Log.e(TAG, "TRASPARENTEEEEEE");
                fillPol.setFillColor(android.R.color.transparent);
                fillPol = null;
            }
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


    private void setMarkerDelimitedTraderNotify(){
        Log.e(TAG, "setMarkerDelimitedTraderNotify " + listTrader.size());
        for (Pair<User, Pair<Float, Polygon>> trader : listTrader) {
            Log.e(TAG, "                    " + trader.getFirst().toString());
            //Random rnd = new Random();
            //int col = rnd.nextInt(360);
            //Log.d(TAG, "AGGIUNGO IIIIIIIIIII MARKERRRRRRRRRRRRRRRRRRR" + new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()));
            if (trader.getFirst().getTraderposition() != null) {
                getmMap().addMarker(new MarkerOptions()
                        .position(new LatLng(trader.getFirst().getTraderposition().getLatitude(), trader.getFirst().getTraderposition().getLongitude()))
                        .title(trader.getFirst().getShopname())
                        .icon(BitmapDescriptorFactory.defaultMarker(trader.getSecond().getFirst()))
                        .snippet("Negozio di: " + trader.getFirst().getSourname() + " " + trader.getFirst().getName()));
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
                float [] col = new float[] { trader.getSecond().getFirst(), 1.0f, 1.0f };
                Log.e(TAG, trader.getSecond().getFirst() + "         " + col[0]);
                polygon.setStrokeColor(Color.HSVToColor(col));
                //listTraderPolygon.add(new Pair<>(trader.first, polygon));
                trader.getSecond().setSecond(polygon);

                if(UserClient.getRun()!=null){
                    mNotifyDelimitedArea = new MyNotify(context, "delimitedAreaChannel", "Uscita dall'area limitata", "Avvisa l'utente dell'uscita dall'area limitata","Attenzione!","Hai oltrepassato l'area limitata!", R.drawable.ic_not_permitted);
                    mNotifySpeed = new MyNotify(context, "speedChannel", "Velocità elevata", "Avvisa l'utente della velocità troppo elevata","Attenzione!","Stai andando troppo veloce!", R.drawable.ic_not_permitted);
                    createNotification(UserClient.getRun(), mNotifyDelimitedArea.getNotify(),mNotifySpeed.getNotify());
                    timerDelimitedArea.start();
                }
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

        timerDelimitedArea = new CountDownTimer(time,10000) {
            @Override
            public void onTick(long millisUntilFinished) {

                if(run != null && run.getGeoPoint() != null && listTrader.get(0).getFirst().getDelimited_area() != null){

                    LatLng position=new LatLng(run.getGeoPoint().getLatitude(),run.getGeoPoint().getLongitude());

                    List<LatLng> latLngs = new ArrayList<>();
                    for (GeoPoint a: listTrader.get(0).getFirst().getDelimited_area()) {
                        latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
                    }

                    Log.e(TAG,"ENTRATO QUA: pre notifica");

                    if(!PolyUtil.containsLocation(position, latLngs,true)){

                        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                        // notificationId is a unique int for each notification that you must define
                        mNotifyDelimitedArea.getNotificationManager().notify(delimitedAreaNotificationID, dilimitedAreaNotification);
                    }

                    //notifica di velocità
                    //bisogna fare una query per prendere la velocità del veicolo
                    if(run.getSpeed()>vehicleRun.getMaxSpeedKMH()){
                        Log.e(TAG,"ENTRATO QUA: notifica velocità");
                        mNotifySpeed.getNotificationManager().notify(speedNotificationID,speedNotification);
                    }
                }

                Log.e(TAG,"TICK TIMER");
            }

            @Override
            public void onFinish() {

            }
        };

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
