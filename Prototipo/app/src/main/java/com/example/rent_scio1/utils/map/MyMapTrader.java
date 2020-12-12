package com.example.rent_scio1.utils.map;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.Run;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.example.rent_scio1.utils.Vehicle;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyMapTrader extends MyMap{

    private User mTrader;
    private static final String TAG = "MyMapTrader";
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private GoogleMap mMap;
    //private ArrayList<ClusterMarkers> listMarker = new ArrayList<>();
    private HashMap<String, Marker> listMarker = new HashMap<>();
    private ClusterManager<ClusterMarkers> clusterManager;
    private Context context;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        mMap = googleMap;
        getUserDetails(googleMap);
        areaLimitata();
        //setUpClusterer();
        searchCustomers();

    }


    public MyMapTrader(Context context) {
        this.context = context;
    }

    private void setUpClusterer() {
        // Position the map.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                UserClient.getUser().getTraderposition().getLatitude(),
                UserClient.getUser().getTraderposition().getLatitude()), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<>(context, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        // Add cluster items (markers) to the cluster manager.
        //addItems();

        searchCustomers();
    }

    private void addItems() {

        // Set some lat/lng coordinates to start with.
        //double lat = 51.5145160;
        //double lng = -0.1270060;

        // Add ten cluster items in close proximity, for purposes of this example.
        /*for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            ClusterMarkers offsetItem = new ClusterMarkers(lat, lng, "Title " + i, "Snippet " + i);
            clusterManager.addItem(offsetItem);
        }*/
    }

    private void searchCustomers(){
        FirebaseFirestore.getInstance().collection("run")
                .whereEqualTo("trader", UserClient.getUser().getUser_id())
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    /*ArrayList<Run> runs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        runs.add(doc.toObject(Run.class));
                    }*/

                    //viewCustomers(runs);

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
                                            Log.e(TAG, "OK");
                                            Marker costumer=mMap.addMarker(new MarkerOptions()
                                                    .position( new LatLng(
                                                            UserClient.getUser().getTraderposition().getLatitude(),
                                                            UserClient.getUser().getTraderposition().getLongitude()))
                                                    .title(user.getName() + " " + user.getSourname())
                                                    .snippet(dc.getDocument().toObject(Run.class).getUser())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.logo1)));

                                            costumer.setVisible(false);

                                            listMarker.put(dc.getDocument().toObject(Run.class).getUser(),costumer);

                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                        }
                                    } else {
                                        Log.w(TAG, "Error getting documents.", task.getException());
                                    }
                                });

                                /*.add(mMap.addMarker(new MarkerOptions()
                                        .position( new LatLng(
                                                UserClient.getUser().getTraderposition().getLatitude(),
                                                UserClient.getUser().getTraderposition().getLongitude()))
                                        .title(dc.getDocument().toObject(Run.class).getUser())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.logo1))));*/

                                //Aggiunto il marker alla lista di marker
                                /*clusterManager.getClusterMarkerCollection().addMarker(new MarkerOptions()
                                        .position( new LatLng(
                                                UserClient.getUser().getTraderposition().getLatitude(),
                                                UserClient.getUser().getTraderposition().getLongitude()))
                                        .title(dc.getDocument().toObject(Run.class).getUser())
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.logo1))
                                );*/

                                /*clusterManager.addItem(new ClusterMarkers(
                                        UserClient.getUser().getTraderposition().getLatitude(),
                                        UserClient.getUser().getTraderposition().getLongitude(),
                                        dc.getDocument().toObject(Run.class).getUser()));*/


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
            //clearMarkers();

//            for(/*ClusterMarkers*/ Marker m : listMarker/*clusterManager.getClusterMarkerCollection().getMarkers()*/){
//                Log.e(TAG, m.getTitle());
//                if(m.getSnippet().equals(run.getUser())){
//                    Log.e(TAG, "modificato");
//                    m.setPosition(new LatLng(run.getGeoPoint().getLatitude(), run.getGeoPoint().getLongitude()));
//                }
//            }
            Marker costumer=listMarker.get(run.getUser());
            costumer.setPosition(new LatLng(run.getGeoPoint().getLatitude(), run.getGeoPoint().getLongitude()));
            costumer.setVisible(true);

            /*listMarker.add(mMap.addMarker(new MarkerOptions()
                    .position( new LatLng(r.getGeoPoint().getLatitude(), r.getGeoPoint().getLongitude()))
                    .title(r.getUser())));*/
        }
    }

    private void clearMarker(Run run){
//        for(Marker m : listMarker/*clusterManager.getClusterMarkerCollection().getMarkers()*/){
//            if(m.getSnippet().equals(run.getUser())){
//                Log.e(TAG, "rimosso");
//                listMarker.remove(m);
//                //clusterManager.removeItem(m);
//                m.remove();
//                //clusterManager.getClusterMarkerCollection().getMarkers().remove(m);
//            }
//        }
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
                    /*mTrader.setGeoPoint(user.getTraderposition());*/
                    UserClient.setUser(user);
                    setCameraView(googleMap);
                }
            });
        }
    }

    private void setCameraView(GoogleMap googleMap){
        double bottomBundary = mTrader.getTraderposition().getLatitude() - .01;
        double leftBoundary = mTrader.getTraderposition().getLongitude() - .01;
        double topBoundary = mTrader.getTraderposition().getLatitude()  + .01;
        double rightBoundary = mTrader.getTraderposition().getLongitude() + .01;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));

        googleMap.addMarker(new MarkerOptions()
                .position( new LatLng(mTrader.getTraderposition().getLatitude(), mTrader.getTraderposition().getLongitude()))
                .title("Tu sei qui!"));
    }

    private void areaLimitata(){
        if(UserClient.getUser()!=null && UserClient.getUser().getDelimited_area()!=null){
            List<LatLng> latLngs = new ArrayList<>();

            List<GeoPoint> geoPoints = UserClient.getUser().getDelimited_area();
            for (GeoPoint a:geoPoints) {
                latLngs.add(new LatLng(a.getLatitude(),a.getLongitude()));
            }

            PolygonOptions polygonOptions=new PolygonOptions().addAll(latLngs).clickable(true);
            Polygon polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(Color.BLACK);
        }
    }
}
