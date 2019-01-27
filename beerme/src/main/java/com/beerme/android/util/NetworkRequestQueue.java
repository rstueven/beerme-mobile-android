package com.beerme.android.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkRequestQueue {
    private static RequestQueue mInstance;

    public static void init(Context context) {
        Log.d("beerme", "NetworkRequestQueue.init()");
        if (mInstance == null) {
            mInstance = Volley.newRequestQueue(context);
        }
    }

    public static RequestQueue getRequestQueue() {
        Log.d("beerme", "NetworkRequestQueue.getRequestQueue()");
        return mInstance;
    }

    public static <T> void addToRequestQueue(Request<T> request) {
        Log.d("beerme", "NetworkRequestQueue.addToRequestQueue()");
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getRequestQueue().add(request);
    }
}