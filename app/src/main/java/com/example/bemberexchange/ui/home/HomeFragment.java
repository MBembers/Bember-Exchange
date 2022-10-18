package com.example.bemberexchange.ui.home;

import static com.example.bemberexchange.Helpers.countryCodeToEmoji;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bemberexchange.CurrencyAdapter;
import com.example.bemberexchange.CurrencyAdapterItem;
import com.example.bemberexchange.R;
import com.example.bemberexchange.RequestQueueManager;
import com.example.bemberexchange.databinding.FragmentHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private JSONObject symbols;
    private ArrayList<String> codes = new ArrayList<String>();
    private ArrayAdapter<String> codesSpinnerAdapter;
    private CurrencyAdapter currencyAdapter;
    private ArrayList<CurrencyAdapterItem> adapterItemsList = new ArrayList<>();
    private Map<String, String> signs = new HashMap<>();
    private Map<String, Bitmap> flags = new HashMap<>();
    private String selectedCode;
    private double selectedValue = 1.0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddDialog();
            }
        });

        currencyAdapter = new CurrencyAdapter(getContext(), R.layout.list_view_item, adapterItemsList);
        binding.currenciesList.setAdapter(currencyAdapter);

        binding.currenciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSetup(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.editValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals("")){
                    selectedValue = 1;
                }
                else {selectedValue = Double.valueOf(editable.toString());}
                adapterItemsList.forEach((element)-> element.setConversionValue(selectedValue));
                currencyAdapter.notifyDataSetChanged();
            }
        });

        getCurrencies();

        return root;
    }

    private void startAddDialog(){
        if(codes.isEmpty()) return;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Add currency");
        ArrayList<String> spinnerList = new ArrayList<>();
        for (String code: codes) {
            spinnerList.add(countryCodeToEmoji(code) + " " + code);
        }
        dialogBuilder.setItems(spinnerList.toArray(new CharSequence[codes.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                addToAdapter(i);
            }
        });
        dialogBuilder.show();
    }

    private void addToAdapter(int i){
        String code = codes.get(i);
        String url = String.format("https://v6.exchangerate-api.com/v6/a2839df23860276ae274e3a8/enriched/%s/%s", selectedCode, code);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject targetData = response.getJSONObject("target_data");
                            String symbol = (String) targetData.get("display_symbol");
                            String flagUrl = (String) targetData.get("flag_url");
                            StringBuilder builder = new StringBuilder();
                            for(String s : symbol.split(","))
                                builder.append((char) Integer.parseInt(s, 16));
                            symbol = builder.toString();
                            double conversionRate = (Double) response.get("conversion_rate");

                            CurrencyAdapterItem currencyAdapterItem = new CurrencyAdapterItem(code,
                                    targetData.getString("currency_name"), conversionRate,
                                    selectedCode, symbol, selectedValue);

                            signs.put(code, symbol);
                            adapterItemsList.add(currencyAdapterItem);
                            currencyAdapter.notifyDataSetChanged();
                            Bitmap flag = flags.get(code);
                            if(flag != null){
                                currencyAdapterItem.setFlagBitmap(flag);
                                currencyAdapter.notifyDataSetChanged();
                                return;
                            }

                            // Initialize a new ImageRequest
                            ImageRequest imageRequest = new ImageRequest(
                                    flagUrl, // Image URL
                                    new Response.Listener<Bitmap>() { // Bitmap listener
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            flags.put(code, response);
                                            currencyAdapterItem.setFlagBitmap(response);
                                            currencyAdapter.notifyDataSetChanged();
                                        }
                                    },
                                    240, // Image width
                                    200, // Image height
                                    ImageView.ScaleType.CENTER_CROP, // Image scale type
                                    Bitmap.Config.RGB_565, //Image decode configuration
                                    new Response.ErrorListener() { // Error listener
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // Do something with error response
                                            error.printStackTrace();
                                            Toast.makeText(getContext(), "flag error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                            RequestQueueManager.getInstance(getContext()).addToRequestQueue(imageRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                    }
                });
        RequestQueueManager.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void selectedSetup(int i) {
        String code = codes.get(i);
        String sign = signs.get(code);
        Bitmap flag = flags.get(code);
        selectedCode = code;
        if(sign != null && flag != null){
            binding.currencySign.setText(sign);
//            binding.selectedFlag.setImageBitmap(flag);
            return;
        }

        String url = String.format("https://v6.exchangerate-api.com/v6/a2839df23860276ae274e3a8/enriched/GBP/%s", code);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String symbol = (String) response.getJSONObject("target_data").get("display_symbol");
                            String flagUrl = (String) response.getJSONObject("target_data").get("flag_url");
                            StringBuilder builder = new StringBuilder();
                            for(String s : symbol.split(","))
                                builder.append((char) Integer.parseInt(s, 16));
                            symbol = builder.toString();
                            signs.put(code, symbol);
                            binding.currencySign.setText(symbol);
                            updateAdapterItemsList();
                            // Initialize a new ImageRequest
                            ImageRequest imageRequest = new ImageRequest(
                                    flagUrl, // Image URL
                                    new Response.Listener<Bitmap>() { // Bitmap listener
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            // Do something with response
//                                            binding.selectedFlag.setImageBitmap(response);
                                            flags.put(code, response);
                                        }
                                    },
                                    0, // Image width
                                    0, // Image height
                                    ImageView.ScaleType.CENTER_CROP, // Image scale type
                                    Bitmap.Config.RGB_565, //Image decode configuration
                                    new Response.ErrorListener() { // Error listener
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // Do something with error response
                                            error.printStackTrace();
                                            Toast.makeText(getContext(), "flag error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                            RequestQueueManager.getInstance(getContext()).addToRequestQueue(imageRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                    }
                });
        RequestQueueManager.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }



    private void getCurrencies(){
        String url = "https://api.exchangerate.host/symbols";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            symbols = response.getJSONObject("symbols");
                            Log.d("XXX", "onResponse: " + symbols.toString().substring(0, 500));
                            symbols.keys().forEachRemaining(codes::add);
                            codes.remove("BTC");
                            ArrayList<String> spinnerList = new ArrayList<>();
                            for (String code: codes) {
                                spinnerList.add(countryCodeToEmoji(code) + " " + code);
                            }
                            codesSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerList);
                            binding.currenciesSpinner.setAdapter(codesSpinnerAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });
        RequestQueueManager.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void updateAdapterItemsList(){
        for (CurrencyAdapterItem currencyAdapterItem:
             adapterItemsList) {

            String url = String.format("https://v6.exchangerate-api.com/v6/a2839df23860276ae274e3a8/enriched/%s/%s",
                    selectedCode , currencyAdapterItem.getCode());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject targetData = response.getJSONObject("target_data");
                                double conversionRate = (Double) response.get("conversion_rate");
                                currencyAdapterItem.setConversionRate(conversionRate);
                                currencyAdapterItem.setConversionValue(selectedValue);
                                currencyAdapterItem.setConversionCode(selectedCode);
                                currencyAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                        }
                    });
            RequestQueueManager.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}