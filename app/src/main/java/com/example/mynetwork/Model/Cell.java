package com.example.mynetwork.Model;

import com.google.gson.annotations.SerializedName;

public class Cell {
    @SerializedName("radio")
    private String radio;
    @SerializedName("mcc")
    private int mcc;
    @SerializedName("mnc")
    private int mnc;
    @SerializedName("area")
    private int area;
    @SerializedName("cid")
    private int cid;
    @SerializedName("range")
    private int range;
    @SerializedName("lat")
    private double lat;
    @SerializedName("lon")
    private double lon;

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
