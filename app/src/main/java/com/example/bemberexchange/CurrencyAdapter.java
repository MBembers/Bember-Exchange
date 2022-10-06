package com.example.bemberexchange;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends ArrayAdapter {
    private ArrayList<String> _list;
    private Context _context;
    private int _resource;

    public CurrencyAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);

//        this._list = objects;
        this._context = context;
        this._resource = resource;
    }
}
