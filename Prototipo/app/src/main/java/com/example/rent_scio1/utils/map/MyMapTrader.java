package com.example.rent_scio1.utils.map;

import android.graphics.Color;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;

public class MyMapTrader extends MyMap{

    private User mTrader;
    private static final String TAG = "MyMapTrader";
    private FirebaseFirestore mStore = FirebaseFirestore.getInstance();
    private GoogleMap mMap;


    //TODO: VISUALIZZAZIONE DELLA MAPPA


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        mMap = googleMap;
        getUserDetails(googleMap);
        areaLimitata();
        //METODO
    }

    

    private void getUserDetails(GoogleMap googleMap){
        if(mTrader == null){
            mTrader = new User();
            DocumentReference userRef = mStore.collection("users").document(FirebaseAuth.getInstance().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: successfully get teh user details");
                        User user = task.getResult().toObject(User.class);
                        mTrader=new User(user);
                        /*mTrader.setGeoPoint(user.getTraderposition());*/
                        UserClient.setUser(user);
                        setCameraView(googleMap);
                    }
                }
            });
        }
    }

    private void setCameraView(GoogleMap googleMap){
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
