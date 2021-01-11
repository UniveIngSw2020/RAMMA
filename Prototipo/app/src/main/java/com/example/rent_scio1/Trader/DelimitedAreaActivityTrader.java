package com.example.rent_scio1.Trader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.PositionIterable;
import com.example.rent_scio1.utils.User;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.PolyUtil;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;


public class DelimitedAreaActivityTrader extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private FirebaseFirestore mStore;

    private static final String TAG = "DelimitedAreaActivityTrader";

    private Polygon polygon=null;

    private PositionIterable markers =new PositionIterable();

    private final Stack<Marker> markersStack =new Stack<>();


    //posizione del trader
    private Marker trader;

    private Toolbar map_trader_delim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delimited_area_trader);

        initViews();

        mStore = FirebaseFirestore.getInstance();

        //inizializzazione mappa 1
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDelimiterSettaArea);
        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirm_changes_limited_area, menu);
        map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
        map_trader_delim.getMenu().findItem(R.id.how_to).setVisible(true);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.confirm_changes_limited:

                if(polygon==null || PolyUtil.containsLocation(trader.getPosition(),polygon.getPoints(),true)){
                    storeDelimitedArea();

                    Intent intent=new Intent(getApplicationContext(), MapsActivityTrader.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(),"L'area limitata deve contenere il tuo negozio",Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.how_to:
                startActivity(new Intent(getApplicationContext(), InfoTutorialDelimitedAreaTrader.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void storeDelimitedArea(){

        User u= UserClient.getUser();
        List<GeoPoint> geoPoints=markers.geoPointList();

        if(geoPoints.size()==0){
            u.setDelimited_area(null);
        }
        else{
            u.setDelimited_area(geoPoints);
        }

        DocumentReference locationRef = mStore
                .collection("users")
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

        locationRef.set(u).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(TAG, "OK, delimited area pushata");
            }
        });
    }

    //inizializzazione mappa 2
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //quando la mappa è caricata setto la visualizzazione
        setCameraView();

        AtomicReference<Marker> lastClickedMarker = new AtomicReference<>();

        mMap.setOnMapClickListener(latLng -> {

            //se nessun marker è stato cliccato in precedenza ne aggiungo uno sulla mappa, altrimenti nascondo la info window
            if(lastClickedMarker.get()==null) {
                //aggiungi marker a mappa e settalo draggable
                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                marker.setDraggable(true);

                //aggiungi marker a list e stack
                markersStack.push(marker);
                markers.add(marker);

                //prova a costruire l'area
                costruisci();
            }
            else{
                //nascondi la info window
                lastClickedMarker.get().hideInfoWindow();
                //setta il marker cliccato a null
                lastClickedMarker.set(null);
            }
        });

        //setto l'info window con il bottone
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapterDelimited(this));

        //setto il comportamento al click del marker
        mMap.setOnMarkerClickListener(marker -> {

            marker.showInfoWindow();
            lastClickedMarker.set(marker);
            return true;
        });

        //setto i listener al click della info window
        mMap.setOnInfoWindowClickListener(marker -> {

            markers.remove(marker);
            markersStack.remove(marker);
            marker.remove();
            lastClickedMarker.set(null);
            costruisci();
        });
        mMap.setOnInfoWindowLongClickListener(marker -> {

            markers.remove(marker);
            markersStack.remove(marker);
            marker.remove();
            lastClickedMarker.set(null);
            costruisci();

        });

        //aggiungi il listener per il drag del marker
        mMap.setOnMarkerDragListener(this);

        //aggiungi MARKER NEGOZIO
        addNegozio();

        //Carica area limitata precedente(se presente)
        //carica il mio array di markers e li aggiunge alla mappa
        List<GeoPoint> geoPoints=UserClient.getUser().getDelimited_area();
        if(geoPoints!=null){
            markers=new PositionIterable(geoPoints,mMap);
            costruisci();
            markersStack.addAll(markers.getMarkers());
        }

    }

    //metodi per l'aggiunta icona negozio
    public Bitmap resizeMapIcons(String filePath, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);

        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private Bitmap getBitmap() {
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.negozio_vettorizzato);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    //aggiungi marker negozio
    private void addNegozio(){

        GeoPoint traderpos=UserClient.getUser().getTraderPosition();
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();

        MarkerOptions markerOptions=new MarkerOptions().position(new LatLng(traderpos.getLatitude(),traderpos.getLongitude())).title("Tu sei qui");

        StorageReference islandRef = mStorageRef.child("users/" + UserClient.getUser().getUser_id() + "/avatar.jpg");
        File localFile;
        try {
            localFile = File.createTempFile("images", "jpg");
            islandRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {

                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(localFile.getPath(),150,150)));
                        trader=mMap.addMarker(markerOptions);

                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "NON caricata");
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(  Bitmap.createScaledBitmap( getBitmap(),150,150,false) ));
                        trader=mMap.addMarker(markerOptions);
                    });


        } catch (IOException ioException) {
            Log.e(TAG, "Errore nel caricamento dell'immaigne");
            ioException.printStackTrace();
        }


    }

    //metodo per costruzione area
    private void costruisci(){

        if(markers.size()>=3){

            //cancella il poligono precedente
            if( polygon!=null ){
                polygon.remove();
                polygon=null;
            }

            //ordina i markers in senso orario
            markers.sort();

            //aggiungi il nuovo poligono
            PolygonOptions polygonOptions=new PolygonOptions().addAll(markers).clickable(true);
            polygon=mMap.addPolygon(polygonOptions);
            polygon.setStrokeColor(getColor(R.color.stroke_delimited));
            polygon.setFillColor(Color.argb(130, 111,163,167));

            //setta a visibile il bottone per confermare i cambiamenti
            map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(true);
        }
        else {
            map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
        }
    }

    @SuppressLint("NonConstantResourceId")
    private void initViews(){
        map_trader_delim = findViewById(R.id.toolbar_map_trader_delimiter);
        setSupportActionBar(map_trader_delim);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //setto il funzionamento dei tasti di eliminazione
        BottomNavigationView bottomNavigationView = findViewById(R.id.gridview_maps_client);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.clear_last:
                    if(!markersStack.empty()){

                        //tolgo dallo stack il primo
                        Marker last=markersStack.pop();

                        //elimino dalla mappa e dalla lista
                        last.remove();
                        markers.remove(last);

                        //se ci sono abbastanza marker costruisci altrimenti elimina
                        if(markers.size()>=3){
                            costruisci();
                        }
                        else{
                            if(polygon!=null){
                                polygon.remove();
                                polygon=null;
                                map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(false);
                            }
                        }

                        //se non ci sono marker sulla mappa setta la possibilità di confermare i cambiamenti
                        if(markersStack.empty())
                            map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(true);
                    }
                    break;
                case R.id.clear_all:

                    //elimina tutti i marker da lista e stack
                    markers.removeAll();
                    markersStack.removeAllElements();

                    //elimino i marker dalla mappa
                    mMap.clear();

                    //setto il poligono a NULL
                    polygon=null;

                    //aggiungo il negozio
                    addNegozio();

                    //setta la possibilità di confermare i cambiamenti
                    map_trader_delim.getMenu().findItem(R.id.confirm_changes_limited).setVisible(true);
                    break;
            }
            return true;
        });

    }

    //metodo per settare la visualizzazione della camera sul negozio
    private void setCameraView(){

        User mTrader=UserClient.getUser();

        double bottomBoundary = mTrader.getTraderPosition().getLatitude() - .01;
        double leftBoundary = mTrader.getTraderPosition().getLongitude() - .01;
        double topBoundary = mTrader.getTraderPosition().getLatitude() + .01;
        double rightBoundary = mTrader.getTraderPosition().getLongitude() + .01;

        LatLngBounds mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary,width,height, padding));

    }


    //metodi per il controllo del drag marker
    @Override
    public void onMarkerDragStart(Marker marker) {
        //tolgo il marker attuale
        markers.remove(marker);
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //aggiungo il marker attuale e tento la costruzione
        markers.add(marker);
        costruisci();
    }
}