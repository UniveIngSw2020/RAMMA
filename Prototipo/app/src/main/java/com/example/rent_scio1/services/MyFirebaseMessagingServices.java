package com.example.rent_scio1.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.MapsActivityTrader;
import com.example.rent_scio1.utils.RequestQueueSingleton;
import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingServices extends FirebaseMessagingService{
    private final String TAG = "MyFirebaseMessagingServices";
    private final String ADMIN_CHANNEL_ID ="admin_channel";
//    private String ADMIN_CHANNEL_ID = "admin_channel";


    @Override
    public void onCreate() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    sendRegistrationToServer(token);
                    // Log and toast
                    Log.d(TAG, "TOKEN: "+token);
                });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.e(TAG, "caspiterina è arrivato un messaggio");
//        if(UserClient. getUser() != null) {
//            String title = remoteMessage.getData().get("title");
//            String type = remoteMessage.getData().get("type");
//            String msg = remoteMessage.getData().get("message");
//            int id = Integer.parseInt(remoteMessage.getData().get("id"));
//            Log.e(TAG, "caspiterina è arrivato un messaggio" + title + " " + type + " " + msg + " " + id);
//            MyNotify notification = new MyNotify(MapsActivityTrader.thisContext, type, title, "prova", title, msg, R.drawable.ic_not_permitted);
//            Log.e(TAG, "" + notification.getNotify());
//            notification.getNotificationManager().notify(id, notification.getNotify());
//        }
        final Intent intent = new Intent(this, MapsActivityTrader.class);
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this , 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_not_permitted);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_not_permitted)
                .setLargeIcon(largeIcon)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        //Set notification color to match your app color template
        //notificationBuilder.setColor(Color.rgb(1,1,1));
        notificationManager.notify(notificationID, notificationBuilder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to devie notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        if(UserClient.getUser() != null && UserClient.getUser().addToken(refreshedToken)) {
           // FirebaseMessaging.getInstance().subscribeToTopic(refreshedToken); // forse è brutto, sarebbe meglio l'id dell'utente
            DocumentReference mDatabase = FirebaseFirestore.getInstance().collection("users").document(UserClient.getUser().getUser_id());
            mDatabase.update("tokens", UserClient.getUser().getTokens()).addOnSuccessListener(aVoid -> Log.d(TAG, "TOKEN AGGIUNTO"));
        }
    }

    public static void sendNotification(Context context, String destinationTopic, String title, String msg) {
        final String TAG = "sendNotification";
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        String topic = "/topics/"+destinationTopic;
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try{
            notificationBody.put("title", title);
            notificationBody.put("message", msg);
            notification.put("to", destinationTopic);
            notification.put("data", notificationBody);
        }catch (JSONException e){
            Log.e("sendNotification", "onCreate: " + e.getMessage() );
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                FCM_API,
                notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> Log.i(TAG, "onErrorResponse: Didn't work"))
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key="+ "AAAABf5BCzc:APA91bGIHRPUPEuWQT0TT5iXwZytmeN_5-NHn2lA_Fpkz3Mmxmxn5i5N3c1wrEoZW7Zoj05_18zNg2KIa5VXuZMwWoLn9_Yzh-hfqXPl-xInE0cDHyDJn0VHdow3cUaxnx-SWCLhtQO3"); //
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);

    }

}
