package com.example.bemberexchange;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class RequestQueueManager {
    RequestQueue queue;

    public RequestQueueManager(Context context){
        queue = Volley.newRequestQueue(context);
    }

    void addToRequestQueue(JsonObjectRequest jsonObjectRequest){
        queue.add(jsonObjectRequest);
    }

}
