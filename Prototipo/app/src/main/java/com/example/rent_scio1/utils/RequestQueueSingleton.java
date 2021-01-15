package com.example.rent_scio1.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton utilizzato per gestire l'invio di messaggi in uscita
 * */
public class RequestQueueSingleton {
    private  static RequestQueueSingleton instance;
    private RequestQueue requestQueue;
    private Context ctx;

    private RequestQueueSingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new RequestQueueSingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
