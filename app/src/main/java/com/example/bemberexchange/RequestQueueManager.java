package com.example.bemberexchange;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class RequestQueueManager {
    private static RequestQueueManager requestQueueManager;

    RequestQueue queue;

    public RequestQueueManager(Context context){
        queue = Volley.newRequestQueue(context);
    }

    public void addToRequestQueue(JsonObjectRequest jsonObjectRequest){
        queue.add(jsonObjectRequest);
    }

    public static RequestQueueManager getInstance(Context _context) {
        if(requestQueueManager == null){
            requestQueueManager = new RequestQueueManager(_context);
        }
        return requestQueueManager;
    }

}
