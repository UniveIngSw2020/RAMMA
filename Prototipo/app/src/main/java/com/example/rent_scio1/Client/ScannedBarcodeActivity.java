package com.example.rent_scio1.Client;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.rent_scio1.R;
import com.example.rent_scio1.services.MyLocationService;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;

/**
 * Activity che apre la fotocamera per scannerizzare il codice QR
 * */
public class ScannedBarcodeActivity extends AppCompatActivity {

    private static final String TAG = "ScannedBarcodeActivity";
    private static final String ToQR="QR_code_creation";
    SurfaceView surfaceView;

    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    public Action event;

    public enum Action{
        DELETE,
        ADD
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        event = (Action) getIntent().getSerializableExtra(ToQR);
    }

    private void initViews() {

        surfaceView = findViewById(R.id.surfaceView);

    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Scansiona il QR!", Toast.LENGTH_SHORT).show();

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                    if (cameraSource != null) {
                        try {
                            cameraSource.release();
                            cameraSource = null;
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            /**
             * Evento chiave che indentifica il rilevamento di un QR
             * */
            @Override
            public void receiveDetections(@NotNull Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    String rawValue = barcodes.valueAt(0).rawValue;
                    String message = "Stai leggendo il QR sbagliato";
                    int length = rawValue.split(" ").length;
                    Log.w(TAG, rawValue);

                    Log.w(TAG, "PRE SWITCH");

                    switch (event){
                        case ADD:       // Casistica per creare una nuova corsa
                            Log.e(TAG, "SWITCH ADD");
                            if(length == 1){
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
                            }else {
                                startLocationService(rawValue);

                                finishAffinity();
                                Intent intent = new Intent(getApplicationContext(), MapsActivityClient.class);
                                startActivity(intent);
                            }
                            break;
                        case DELETE:    // Casistica per eliminare una nuova corsa
                            Log.e(TAG, "SWITCH DELETE");
                            Log.e(TAG, ""+UserClient.getRun());
                            //Log.e(TAG, "SWITCH DELETE           " + UserClient.getRun().getRunUID());
                            if(UserClient.getRun() != null){
                                if(length == 1 && rawValue.equals(UserClient.getRun().getRunUID())) {

                                    Intent serviceIntent=new Intent(getApplicationContext(), MyLocationService.class);
                                    serviceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                    stopService(serviceIntent);

                                    unlockVehiclebyID(UserClient.getRun().getVehicle());

                                    deleteRun(rawValue);
                                }else{
                                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
                                }
                            }
                            break;
                        default:
                            Log.w(TAG, "SWITCH DEFAULT...");
                            break;
                    }


                }
            }
        });
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //TODO VEDERE SE SI PUO' METTERE IL PATH IN AUTOMATICO
            if ("com.example.rent_scio1.services.MyLocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.e(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void startLocationService(String rawValue) {
        if (!isLocationServiceRunning()) {
            Log.e(TAG, "RUNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");

            Intent serviceIntent = new Intent(this, MyLocationService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            serviceIntent.putExtra(TAG, rawValue);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                ScannedBarcodeActivity.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
            if (cameraSource != null) {
                try {
                    cameraSource.release();
                    cameraSource = null;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
        initialiseDetectorsAndSources();
    }

    private void unlockVehiclebyID(String id){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("vehicles").document(id);
        mDatabase.update("rented", false).addOnSuccessListener(aVoid -> Log.d(TAG, "VEICOLO LIBERATO"));
    }


    private void deleteRun(String PK_run){
        FirebaseFirestore.getInstance().collection("run").document(PK_run)
                .delete()
                .addOnSuccessListener(aVoid -> Log.e(TAG, "DocumentSnapshot successfully deleted!"))
                .addOnFailureListener(e -> Log.e(TAG, "Errore CORSA NON ELIMINATA", e))
                .addOnCompleteListener(task -> {
                    UserClient.setRun(null);
                    UserClient.setTrader(null);

                    finishAffinity();
                    Intent intent = new Intent(getApplicationContext(), MapsActivityClient.class);
                    startActivity(intent);

                });

        Log.e(TAG,"AREA ELIMINATA");

    }

}
