package com.example.rent_scio1.utils.map;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.MapsActivityClient;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.MyNotify;
import com.example.rent_scio1.utils.PermissionUtils;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.List;

public class MyMapClient extends MyMap{

    private ArrayList<User> listTrader = new ArrayList<>();
    private static final String TAG = "MyMapClient";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Context context;
    private MyNotify mNotify;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLngBounds mMapBoundary;
    //private Polygon delimitedArea;

    public MyMapClient(){
        super();
    }

    public MyMapClient(Context context){
        super();
        this.context = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /*
    1- in commerciante solo la sua
    2- in clinete senza corsda tutte
    3- in clinete con corsa attiva solo quella affine
    */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        enableMyLocation();
        getPositionTrader();
        Run run = UserClient.getRun();
        if (run != null) {

            Log.e(TAG, context.toString());
            mNotify = new MyNotify(context, "delimitedAreaChannel", "Uscita dall'area limitata", "Avvisa l'utente dell'uscita dall'area limitata", R.drawable.ic_not_permitted);

            // calcolo il tempo rimanente alla fine della corsa, in questo modo non spreco risorse.
            // nel caso peggiore il cliente non uscirà mai da questa schermata e dovrò aggiornare ogni minuto della corsa.
            long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

            CountDownTimer timerDelimitedArea = new CountDownTimer(time,10000) {
                @Override
                public void onTick(long millisUntilFinished) {

                    //IN QUESTO CASO IL CLIENTE VISUALIZZA IN TEMPO REALE LE MODIFICHE DELL'AREA LIMITATA
                    //SPOSTARE FUORI DALL'EVENTE IL METODO ALTRIMENTI
                    //delimitedArea(run);
                    //TODO volendo si puo' gestire un po' meglio il discorso dell'id di notifica, così da fare azioni quando bla bla bla

                    startNotification(run, mNotify.getNotify(),0);
                    Log.e(TAG,"TICK TIMER");
                }

                @Override
                public void onFinish() {

                }
            }.start();

        }
    }


    private void getPositionTrader(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query getTrader;
        if(UserClient.getRun() != null){
            getTrader = db.collection("users").whereEqualTo("user_id", UserClient.getRun().getTrader());
        }else{
            getTrader = db.collection("users").whereEqualTo("trader", true);
        }
        getTrader.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                listTrader.add(new User(document.toObject(User.class)));
            }
            setMarkerDelimitedTrader();
        });
    }


    private void setMarkerDelimitedTrader(){
        for (User trader : listTrader) {
            //Log.d(TAG, "AGGIUNGO IIIIIIIIIII MARKERRRRRRRRRRRRRRRRRRR" + new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()));
            getmMap().addMarker(new MarkerOptions()
                    .position(new LatLng(trader.getTraderposition().getLatitude(), trader.getTraderposition().getLongitude()))
                    .title(trader.getShopname())
                    .snippet("Negozio di: " + trader.getSourname() + " " + trader.getName()));

            List<LatLng> latLngs = new ArrayList<>();
            if(trader.getDelimited_area() != null){
                //Log.e(TAG, trader.getDelimited_area().toString());
                for (GeoPoint a: trader.getDelimited_area()) {
                    //Log.e(TAG,"Sto creando la lista di latlang");
                    latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
                }
                //Log.e(TAG,"stampo il poligono");
                PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
                Polygon polygon=getmMap().addPolygon(polygonOptions);
                polygon.setStrokeColor(Color.BLACK);
            }
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (getmMap() != null) {
                getmMap().setMyLocationEnabled(true);
                getLastKnownLocation();
                //getUserDetails();
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission((AppCompatActivity) context, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    private void startNotification(Run run, Notification n, int notificationID) {

        //se il commerciante ha impostato un'area limitata attivo le notifiche di posizione non consentita
        if(UserClient.getUser().getDelimited_area()!=null){

            LatLng position=new LatLng(run.getGeoPoint().getLatitude(),run.getGeoPoint().getLongitude());

            List<LatLng> latLngs = new ArrayList<>();
            for (GeoPoint a: listTrader.get(0).getDelimited_area()) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }

            Log.e(TAG,"ENTRATO QUA: pre notifica");
            if(!PolyUtil.containsLocation(position, latLngs,true)){

                //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                // notificationId is a unique int for each notification that you must define
                mNotify.getNotificationManager().notify(notificationID, n);
            }
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

            mMapBoundary = new LatLngBounds(
                    new LatLng(bottomBundary, leftBoundary),
                    new LatLng(topBoundary, rightBoundary)
            );

            getmMap().moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
        } catch (Exception e) {
        }
    }

}
