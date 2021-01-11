package com.example.rent_scio1.utils.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.CustomInfoWindowAdapterTrader;
import com.example.rent_scio1.utils.Clustering.ClusterMarker;
import com.example.rent_scio1.utils.Clustering.MyClusterManagerRenderer;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMapTrader extends MyMap{

    private User mTrader;
    private static final String TAG = "MyMapTrader";

    private final FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    //private GoogleMap mMap;
    private final HashMap<String, ClusterMarker> listMarker = new HashMap<>();

    private final Context context;

    private StorageReference mStorageRef;

    private final Map<String,Run> mapRuns=new HashMap<>();
    private ClusterManager<ClusterMarker> clusterManager = null;
    private MyClusterManagerRenderer mClusterManagerRenderer = null;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);

        getUserDetails(googleMap);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //setCameraView(googleMap);

        clusterManager = new ClusterManager<>(context, getmMap());
        mClusterManagerRenderer = new MyClusterManagerRenderer(context, getmMap(), clusterManager);
        clusterManager.setRenderer(mClusterManagerRenderer);

        getmMap().setOnCameraIdleListener(clusterManager);

        delimitedArea();
        try {
            searchCustomers();
        }catch (Exception e){
            Log.e(TAG, "ERROREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            e.printStackTrace();
        }

        clusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoWindowAdapterTrader(context));
        getmMap().setInfoWindowAdapter(clusterManager.getMarkerManager());



        clusterManager.setOnClusterItemClickListener(item -> {
            if(item.getRunId() != null){
                for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                    if(m.getPosition().equals(item.getPosition())){
                        m.showInfoWindow();
                        Log.e(TAG, "Mostro l'info window sul commerciante");
                    }
                }
            }
            return true;
        });


        getmMap().setOnMarkerClickListener(clusterManager);


        clusterManager.setOnClusterItemInfoWindowClickListener(item -> {
            if(item.getRunId() != null){
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Query getRun = db.collection("run").whereEqualTo("runUID", item.getRunId());
                getRun.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Query getUser = db.collection("users").whereEqualTo("user_id", new Run(document.toObject(Run.class)).getUser());
                        getUser.get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            String phone = "";
                            for (QueryDocumentSnapshot document1 : queryDocumentSnapshots1) {
                                phone = document1.toObject(User.class).getPhone();
                            }
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("tel:" + phone));
                            context.startActivity(intent);
                        });
                    }
                });
            }
        });

        getmMap().setOnInfoWindowClickListener(clusterManager);

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

    private void addDocument(String title, Bitmap image, DocumentChange dc){
        Run run=dc.getDocument().toObject(Run.class);
        ClusterMarker item;
        if(run.getGeoPoint()!=null){
            item = new ClusterMarker(run.getGeoPoint().getLatitude(),run.getGeoPoint().getLongitude(), title, image, run.getRunUID());
        }else{
            item = new ClusterMarker(UserClient.getUser().getTraderPosition().getLatitude(),UserClient.getUser().getTraderPosition().getLongitude(), title, image, run.getRunUID());
        }
        clusterManager.addItem(item);
        clusterManager.cluster();
        long time=run.getStartTime() + run.getDuration() - Calendar.getInstance().getTime().getTime();

        listMarker.put(dc.getDocument().toObject(Run.class).getUser(),item);

        new CountDownTimer(time,1000){
            @Override
            public void onTick(long millisUntilFinished) {

                ClusterMarker item = listMarker.get(run.getUser());


                int minutes=(int) (millisUntilFinished / 1000) / 60;
                int seconds=(int) (millisUntilFinished / 1000) % 60;
                int speed=mapRuns.get(run.getRunUID()).getSpeed();

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

                    String secondText=""+seconds;
                    if(seconds<10){

                        secondText="0"+seconds;
                    }

                    if( item!=null)
                        item.setSnippet(speed+" "+hoursText+":"+minutesText+":"+secondText );

                    //Log.e(TAG, "                                                                                                " + item.getTitle());

                }else{

                    String minutesText=""+minutes;
                    if(minutes<10){
                        minutesText="0"+minutesText;
                    }

                    String secondText=""+seconds;
                    if(seconds<10){

                        secondText="0"+seconds;
                    }

                    if( item!=null)
                        item.setSnippet(speed+" "+minutesText+":"+secondText );

                    //Log.e(TAG, "                                                                                                " + item.toString());

                }
                mClusterManagerRenderer.setUpdateInfoWindow(item);

                clusterManager.cluster();
            }

            @Override
            public void onFinish() {
                ClusterMarker item = listMarker.get(run.getUser());
                if( item!=null)
                    item.setSnippet( run.getSpeed()+" "+"TERMINATO");
                mClusterManagerRenderer.setUpdateInfoWindow(item);
                clusterManager.cluster();
                for(Marker m : clusterManager.getMarkerCollection().getMarkers()){
                    if(( item!=null) && m.getPosition().equals(item.getPosition())){
                        m.showInfoWindow();
                        Log.e(TAG, "Mostro l'info window sul commerciante");
                    }
                }

            }
        }.start();
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

                                Run run=dc.getDocument().toObject(Run.class);
                                mapRuns.put(run.getRunUID(),run);

                                Query getVehiclesTrader = FirebaseFirestore.getInstance().collection("users").whereEqualTo("user_id", dc.getDocument().toObject(Run.class).getUser());

                                getVehiclesTrader.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            User user = new User(document.toObject(User.class));

                                            StorageReference islandRef = mStorageRef.child("users/" + user.getUser_id() + "/avatar.jpg");
                                            Log.e(TAG, "COGNOME: "+user.getSurname());
                                            //MarkerOptions markerOptions=new MarkerOptions().title(user.getName() + " " + user.getSurname());

                                            File localFile;

                                            try {
                                                localFile = File.createTempFile("images", "jpg");
                                                islandRef.getFile(localFile)
                                                        .addOnCompleteListener(taskSnapshot -> {
                                                            Bitmap image;
                                                            if (taskSnapshot.isSuccessful()) {
                                                                image = resizeMapIcons(localFile.getPath(), 100, 100);
                                                            } else {
                                                                image = Bitmap.createScaledBitmap(getBitmap(R.drawable.logo1), 100, 100, false);
                                                            }
                                                            Log.e(TAG, "                                                                                    aggiunto marker al cluster");
                                                            //clusterManager.addItem(new ClusterMarker(run.getGeoPoint().getLatitude(), run.getGeoPoint().getLongitude(), user.getName() + " " + user.getSurname(), image));
                                                            addDocument(user.getName() + " " + user.getSurname(), image,dc);
                                                        });
                                            } catch (IOException e1) {
                                                e1.printStackTrace();
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
            ClusterMarker costumer=listMarker.get(run.getUser());

            if(costumer!=null){

                costumer.setPosition(new LatLng(run.getGeoPoint().getLatitude(), run.getGeoPoint().getLongitude()));
                mClusterManagerRenderer.setUpdateMarker(costumer);
                mapRuns.put(run.getRunUID(),run);
                clusterManager.cluster();
            }

        }
    }

    private void clearMarker(Run run){
        ClusterMarker item = listMarker.get(run.getUser());
        for (Marker m : clusterManager.getMarkerCollection().getMarkers()){
            if(item.getPosition().equals(m.getPosition())){
                m.remove();
                clusterManager.removeItem(item);
            }
        }
        listMarker.remove(run.getUser());
        //item.remove();
        clusterManager.cluster();
    }

    //questo metodo va completamente eliminato
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

        /*
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,width,height, padding));*/

        //questo richiamo va cancellato



        StorageReference islandRef = mStorageRef.child("users/" + UserClient.getUser().getUser_id() + "/avatar.jpg");
        File localFile;
        try {
            localFile = File.createTempFile("images", "jpg");
            islandRef.getFile(localFile)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            clusterManager.addItem(new ClusterMarker(mTrader.getTraderPosition().getLatitude(),
                                    mTrader.getTraderPosition().getLongitude(),
                                    UserClient.getUser().getShopName(),
                                    resizeMapIcons(localFile.getPath(), 150, 150), null));
                        }else{
                            Log.e(TAG, "NON caricata");
                            clusterManager.addItem(new ClusterMarker(mTrader.getTraderPosition().getLatitude(),
                                    mTrader.getTraderPosition().getLongitude(),
                                    UserClient.getUser().getShopName(),
                                    Bitmap.createScaledBitmap(getBitmap(R.drawable.negozio_vettorizzato),150, 158, false), null));
                        }
                        clusterManager.cluster();
                    });


        } catch (IOException ioException) {
            Log.e(TAG, "Errore nel caricamento dell'immaigne");
            ioException.printStackTrace();
        }
    }

    private void delimitedArea(){
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
