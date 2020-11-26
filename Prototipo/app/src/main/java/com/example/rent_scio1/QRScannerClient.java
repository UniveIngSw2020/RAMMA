package com.example.rent_scio1;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rent_scio1.services.LocationService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;*/
//kit ML google per lettore barcode


@Deprecated
class QRScannerClient extends AppCompatActivity {

    Boolean hasAQr= false;
    private Map<String, Object> run = new HashMap<>();
    private static final String TAG = "QRScannerClient";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Intent serviceIntent;

   /*
    lettore precedente

    private IntentIntegrator qrScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scanner_client);

        qrScan = new IntentIntegrator(this);
        qrScan.setOrientationLocked(false);
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
                resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                //scan have an error
            } else {
                //scan is successful
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }*/




    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap inputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scanner_client);
        dispatchTakePictureIntent();
        //Button scanQr= findViewById(R.id.scanQr);



    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            inputImage = (Bitmap) extras.get("data");
            scanBarcodes(InputImage.fromBitmap(inputImage,0));




        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Log.e(TAG, "CIAO");
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, ""+e);
        }
    }

    private void startLocationService(String rawValue) {
        if (!isLocationServiceRunning()) {
            serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra(TAG, rawValue);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                //QRScannerClient.this.startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private void scanBarcodes(InputImage image) {
        // [START set_detector_options]
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        // [END set_detector_options]

        // [START get_detector]
        // BarcodeScanner scanner = BarcodeScanning.getClient();
        // Or, to specify the formats to recognize:
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        // [END get_detector]
        // [START run_detector]
        // Task completed successfully
        // [START_EXCLUDE]
        // [START get_barcodes]
        // See API reference for complete list of supported types
        // [END get_barcodes]
        // [END_EXCLUDE]
        // Task failed with an exception
        // ...
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Task completed successfully
                        // [START_EXCLUDE]
                        // [START get_barcodes]
                        for (Barcode barcode : barcodes) {

                            /*
                            Rect bounds = barcode.getBoundingBox();
                            Point[] corners = barcode.getCornerPoints();

                             */

                            //in rawValue è presente il valore letto dal qr code
                            String rawValue = barcode.getRawValue();

                            /*run.put("fk_trader", rawValue.split(" ")[0]);
                            run.put("fk_vehicle", rawValue.split(" ")[1]);
                            run.put("fk_client", UserClient.getUser().getUser_id());*/

                            db.collection("run")
                                    .add(run)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                            startLocationService(rawValue);

                                            // TODO: TORNARE ALLA MAPSACTIVITYCLIENT
                                            Intent intent=new Intent(getApplicationContext(), MapsActivityClient.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });

                            Log.println( Log.WARN,"RAW","funziona" + rawValue);

                            /*int valueType = barcode.getValueType();
                            // See API reference for complete list of supported types
                            switch (valueType) {
                                case Barcode.TYPE_WIFI:
                                    String ssid = barcode.getWifi().getSsid();
                                    String password = barcode.getWifi().getPassword();
                                    int type = barcode.getWifi().getEncryptionType();
                                    break;
                                case Barcode.TYPE_URL:
                                    String title = barcode.getUrl().getTitle();
                                    String url = barcode.getUrl().getUrl();
                                    break;
                            }*/
                        }
                        // [END get_barcodes]
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        Log.w(TAG, "Error adding document", e);

                    }
                });
        // [END run_detector]

    }
}