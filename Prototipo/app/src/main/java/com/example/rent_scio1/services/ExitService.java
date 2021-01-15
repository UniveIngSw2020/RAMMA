package com.example.rent_scio1.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.rent_scio1.utils.UserClient;
import com.google.firebase.auth.FirebaseAuth;

//classe di servizi, qua terminiamo i servizi di foreground.

public class ExitService extends Service {
    private final String TAG = "ExitService";
    public ExitService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "ExitService partito");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "ExitService partito");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        if(UserClient.getRun() == null)
            FirebaseAuth.getInstance().signOut();
        //stop service
        Log.e(TAG, "ExitService FERMATO");
        this.stopSelf();
    }

}