package com.example.bemberexchange.ui.charts;

import static com.example.bemberexchange.Helpers.countryCodeToEmoji;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.bemberexchange.R;
import com.example.bemberexchange.RequestQueueManager;
import com.example.bemberexchange.databinding.FragmentChartsBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ChartsFragment extends Fragment {

    private FragmentChartsBinding binding;
    private LinearLayout root;
    private String chartType = "Bar";
    private BarChart barChart;
    private LineChart lineChart;
    private JSONObject symbols;
    private ArrayList<String> codes;
    private String mainCurrencyCode;
    private String compareCurrencyCode;
    private Date startDate;
    private Date endDate;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChartsBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        codes = new ArrayList<String>();

        barChart = new BarChart(getContext());
        barChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        lineChart = new LineChart(getContext());
        lineChart.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));


        binding.btnChangeChartType.setOnClickListener(view -> switchChartType());
        binding.btn7day.setOnClickListener(view -> changeTimeFrame("7day"));
        binding.btn14day.setOnClickListener(view -> changeTimeFrame("14day"));
        binding.btn1month.setOnClickListener(view -> changeTimeFrame("1month"));
        binding.btn3month.setOnClickListener(view -> changeTimeFrame("3month"));
        binding.btn6month.setOnClickListener(view -> changeTimeFrame("6month"));
        binding.btn1year.setOnClickListener(view -> changeTimeFrame("1year"));

        getCurrencies();

        binding.mainCurrencySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectCurrency("main", i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.compareCurrencySpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectCurrency("compare", i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        changeTimeFrame("14day");
        switchChartType();
        return root;
    }



    private void changeTimeFrame(String timeFrame){
        endDate = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        switch (timeFrame){
            case "7day":
                calendar.add(Calendar.DATE, -7);
                startDate = calendar.getTime();
                break;
            case "14day":
                calendar.add(Calendar.DATE, -14);
                startDate = calendar.getTime();
                break;
            case "1month":
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
            case "3month":
                calendar.add(Calendar.MONTH, -3);
                startDate = calendar.getTime();
                break;
            case "6month":
                calendar.add(Calendar.MONTH, -6);
                startDate = calendar.getTime();
                break;
            case "1year":
                calendar.add(Calendar.YEAR, -1);
                startDate = calendar.getTime();
                break;
        }
        refreshChart();
    }

    private void refreshChart(){
        if(mainCurrencyCode == null || compareCurrencyCode == null) return;
        String url = String.format(
                "https://api.exchangerate.host/timeseries?start_date=%s&end_date=%s&symbols=%s&base=%s",
                 dateFormat.format(startDate), dateFormat.format(endDate),
                 compareCurrencyCode, mainCurrencyCode
        );
        Log.d("XXX", url);
        Log.d("XXX", "refreshChart: " + mainCurrencyCode + " | " + compareCurrencyCode);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("XXX", "onResponse: " + response.toString().substring(0, 500));
                            JSONObject rates = response.getJSONObject("rates");
                            applyData(rates);
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

    private void applyData(JSONObject rates) {
        ArrayList<MyDataObject> dataObjects = new ArrayList<>();
        Iterator<String> keys = rates.keys();

        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        while(keys.hasNext()){
            String dateString = keys.next();
            labels.add(dateString);

            try {
                JSONObject rate = rates.getJSONObject(dateString);
                double compareRate = rate.getDouble(compareCurrencyCode);
                dataObjects.add(new MyDataObject(i, compareRate));
            }
            catch (JSONException e){
                Toast.makeText(getContext(), "We don't support this currency", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }


            i++;
        }

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        };

        switch (chartType){
            case "Line":
                List<Entry> lineEntries = new ArrayList<>();
                for (MyDataObject data : dataObjects) {
                    lineEntries.add(new Entry(data.x, (float) data.y));
                }

                LineDataSet lineDataSet = new LineDataSet(lineEntries, "Line chart"); // add entries to dataset
                lineDataSet.setColor(R.color.black);
                lineDataSet.setValueTextColor(R.color.black); // styling, ...

                LineData lineData = new LineData(lineDataSet);
                lineChart.setData(lineData);
                XAxis lineChartXAxis = lineChart.getXAxis();
                lineChartXAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                lineChartXAxis.setValueFormatter(formatter);
                lineChart.invalidate(); // refresh
                break;
            case "Bar":
                List<BarEntry> barEntries = new ArrayList<>();
                for (MyDataObject data : dataObjects) {
                    // turn your data into Entry objects
                    barEntries.add(new BarEntry(data.x, (float) data.y));
                }

                BarDataSet barDataSet = new BarDataSet(barEntries, "Bar chart"); // add entries to dataset
                barDataSet.setColor(R.color.black);
                barDataSet.setValueTextColor(R.color.black); // styling, ...

                BarData barData = new BarData(barDataSet);
                barChart.setData(barData);
                XAxis barChartXAxis = barChart.getXAxis();
                barChartXAxis.setGranularity(1f); // minimum axis-step (interval) is 1
                barChartXAxis.setValueFormatter(formatter);
                barChart.invalidate();
                break;
        }
    }

    private class MyDataObject {
            public int x;
            public double y;
            MyDataObject(int x, double y){
                this.x = x;
                this.y = y;
            }
    }

    private void selectCurrency(String currencyType, int i){
        switch (currencyType){
            case "main":
                mainCurrencyCode = codes.get(i);
                break;
            case "compare":
                compareCurrencyCode = codes.get(i);
                break;
        }
        refreshChart();
    }

    private void switchChartType(){
        switch (chartType){
            case "Line":
                chartType = "Bar";
                root.removeView(lineChart);
                root.addView(barChart);
                break;
            case "Bar":
                chartType = "Line";
                root.removeView(barChart);
                root.addView(lineChart);
                break;
        }
        refreshChart();
    }

    private void getCurrencies(){
        String url = "https://api.exchangerate.host/symbols";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            symbols = response.getJSONObject("symbols");
                            symbols.keys().forEachRemaining(codes::add);
                            codes.remove("BTC");
                            ArrayList<String> spinnerList = new ArrayList<>();
                            for (String code: codes) {
                                spinnerList.add(countryCodeToEmoji(code) + " " + code);
                            }
                            ArrayAdapter<String> codesSpinnerAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, spinnerList);
                            binding.mainCurrencySpinner.setAdapter(codesSpinnerAdapter);
                            binding.compareCurrencySpinner.setAdapter(codesSpinnerAdapter);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}