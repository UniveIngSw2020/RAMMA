package com.example.rent_scio1.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
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

public class MyFirebaseMessagingServices extends FirebaseMessagingService{
    private final String TAG = "MyFirebaseMessagingServices";

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
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        sendRegistrationToServer(s);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        if(UserClient.getUser().addToken(refreshedToken)) {
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
//        // See documentation on defining a message payload.
//        RemoteMessage message = new RemoteMessage.Builder(destinationToken+"@gcm.googleapis.com")
//                .addData(msg1, msg2)
//                .build();
//
//        // Send a message to the devices subscribed to the provided topic.
//
//        FirebaseMessaging.getInstance().send(message);
//        // Response is a message ID string.
//        Log.e("SEND NOTIFICATION", "TI PREGO FUNZIONA anche se non so come vedere che funziona :/");
//        // [END send_to_topic]

    }

}
