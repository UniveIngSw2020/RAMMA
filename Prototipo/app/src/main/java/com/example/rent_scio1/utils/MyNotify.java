package com.example.rent_scio1.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class MyNotify {
    private Notification notify;
    private Context context;
    private NotificationManager notificationManager;
    private static String TAG = "MyNotify";

    public MyNotify(Context context, String IDChannel, String channelName, String channelDescriptionNot, String notificationTitle, String notificationText, int icon) {
        this.context = context;
        //Log.e(TAG, "diodiodiodiodio");
        this.notify = createNotificationChannel(IDChannel, channelName, notificationTitle, notificationText, channelDescriptionNot, icon);
    }

    private Notification createNotificationChannel(String IDChannel, String channelName, String channelDescriptionNot, String notificationTitle, String notificationText, int icon) {

        if (Build.VERSION.SDK_INT >= 26) {

            //int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(IDChannel, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            //Log.e(TAG, "NotificationChannel, " + channel.toString());
            channel.setDescription(channelDescriptionNot);

            //Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification not = new NotificationCompat.Builder(context, "delimitedAreaChannel")
                    .setSmallIcon(icon/*R.drawable.ic_not_permitted*/)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT).build();
            //.setAutoCancel(true);
            //.setTimeoutAfter(60000)
            //.setSound(alarmSound);


            //Log.e(TAG, "Notification, " + not.toString());

            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // notificationId is a unique int for each notification that you must define
            //Log.e(TAG, "ENTRATO QUA: createNotificationChannel");

            return not;
        }
        //Log.e(TAG, "RITORNA NULLA LA createNotificationChannel");
        return null;
    }

    public Notification getNotify() {
        return notify;
    }

    public Context getContext() {
        return context;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

//    public void sendNotification(String destinationToken, String title, String msg) {
//
//        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
//
//        JSONObject notification = new JSONObject();
//        JSONObject notificationBody = new JSONObject();
//
//        try {
//            notificationBody.put("title", title);
//            notificationBody.put("message", msg);
//            notification.put("to", destinationToken);
//            notification.put("data", notificationBody);
//        } catch (JSONException e) {
//            Log.e("sendNotification", "onCreate: " + e.getMessage());
//        }
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                FCM_API,
//                notification,
//                response -> Log.i(TAG, "onResponse: " + response.toString()),
//                error -> Log.i(TAG, "onErrorResponse: Didn't work")) {
//            @Override
//            public Map<String, String> getHeaders(){
//                Map<String, String> params = new HashMap<>();
//                params.put("Authorization", "key=" + "AAAABf5BCzc:APA91bGIHRPUPEuWQT0TT5iXwZytmeN_5-NHn2lA_Fpkz3Mmxmxn5i5N3c1wrEoZW7Zoj05_18zNg2KIa5VXuZMwWoLn9_Yzh-hfqXPl-xInE0cDHyDJn0VHdow3cUaxnx-SWCLhtQO3"); //
//                params.put("Content-Type", "application/json");
//                return params;
//            }
//        };
//
//        RequestQueueSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
//
//    }

}