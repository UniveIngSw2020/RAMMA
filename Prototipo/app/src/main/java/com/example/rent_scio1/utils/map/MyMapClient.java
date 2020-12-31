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
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.DataParser;
import com.example.rent_scio1.utils.Pair;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyMapClient extends MyMap {

    private static final String TAG = "MyMapClient";
    private final AppCompatActivity context;
    private ArrayList<Pair<User, Pair<Float, Polygon>>> listTrader;
    private final FusedLocationProviderClient mFusedLocationClient;
    private boolean mLocationPermissionGranted = false;
    private Location actualLocation;

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
                    } else {
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


        getmMap().setOnMapLongClickListener(latLng -> {
            if(UserClient.getRun() != null) {
                getmMap().addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))).setTitle("Clicca per vanigare fin qui");
            }
        });

        getmMap().setOnInfoWindowClickListener(marker -> {
            Log.e(TAG,"Click infowindow");
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setMessage("Vuoi attivare la navigazione?")
                    .setCancelable(true)
                    .setPositiveButton( "Sì", (dialogInterface, i) -> {
                        Log.e(TAG,"SI creo la navigazione");


                        String uri = "http://maps.google.com/maps?saddr=" + actualLocation.getLatitude() + "," + actualLocation.getLongitude() + "&daddr=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        context.startActivity(intent);


                        //TODO: NAVIGAZIONE
                        /*
                        String url = getRequestedUrl(new LatLng(actualLocation.getLatitude(), actualLocation.getLongitude()), marker.getPosition());
                        Log.e("onMapClick", url);
                        FetchUrl FetchUrl = new FetchUrl();
                        FetchUrl.execute(url);
                        */




                        dialogInterface.dismiss();
                    }).setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
            final AlertDialog alert = builder.create();
            alert.show();
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





/*

    private String getRequestedUrl(LatLng origin, LatLng destination) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";

        String param = strOrigin + "&" + strDestination + "&" + sensor + "&" + mode;
        String output = "json";
        String APIKEY = "AIzaSyAufu7FvGg-AIzaSyB4cHCVsFMmJEBrYM1lkNpG_BgwoxMM8vo";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param + APIKEY;
        return url;
    }


    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                getmMap().addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }

*/
}
