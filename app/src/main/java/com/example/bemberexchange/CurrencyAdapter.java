package com.example.bemberexchange;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CurrencyAdapter extends ArrayAdapter {
    private ArrayList<CurrencyAdapterItem> _list;
    private Context _context;
    private int _resource;

    public CurrencyAdapter(@NonNull Context context, int resource, @NonNull ArrayList<CurrencyAdapterItem> currencies) {
        super(context, resource, currencies);

        this._list = currencies;
        this._context = context;
        this._resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate  (_resource, null);

        CurrencyAdapterItem item = _list.get(position);

        TextView codeTextView = convertView.findViewById(R.id.list_code_text);
        TextView nameTextView = convertView.findViewById(R.id.list_name);
        TextView valueTextView = convertView.findViewById(R.id.list_value);
        TextView conversionTextView = convertView.findViewById(R.id.list_conversion);

        codeTextView.setText(item.getCode());
        nameTextView.setText(item.getName());
        valueTextView.setText(item.getValueString());
        conversionTextView.setText(item.getConversionString());

        if(item.getFlagBitmap() != null){
            ImageView flagView = convertView.findViewById(R.id.list_flag);
            flagView.setImageBitmap(item.getFlagBitmap());
        }

        return convertView;
    }
}
