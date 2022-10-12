package com.example.bemberexchange;

import android.graphics.Bitmap;

import java.util.Locale;

public class CurrencyAdapterItem {
    private String code;
    private String name;
    private double conversionRate;
    private String conversionCode;
    private Bitmap flagBitmap = null;
    private String symbol;
    private double conversionValue;

    public CurrencyAdapterItem(String code, String name, double conversionRate, String conversionCode, String symbol, double conversionValue) {
        this.code = code;
        this.name = name;
        this.conversionRate = conversionRate;
        this.conversionCode = conversionCode;
        this.symbol = symbol;
        this.conversionValue = conversionValue;
    }

    public String getValueString(){
        return String.format("%s %f", symbol, conversionRate * conversionValue);
    }

    public String getConversionString(){
        return String.format("1 %s = %f %s", code, 1 / conversionRate, conversionCode);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public String getConversionCode() {
        return conversionCode;
    }

    public void setConversionCode(String conversionCode) {
        this.conversionCode = conversionCode;
    }

    public Bitmap getFlagBitmap() {
        return flagBitmap;
    }

    public void setFlagBitmap(Bitmap flagBitmap) {
        this.flagBitmap = flagBitmap;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public double getConversionValue() {
        return conversionValue;
    }

    public void setConversionValue(double conversionValue) {
        this.conversionValue = conversionValue;
    }

}
