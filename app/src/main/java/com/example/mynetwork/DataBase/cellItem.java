package com.example.mynetwork.DataBase;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Cell")
public class cellItem {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "cid")
    private int cid;
    @ColumnInfo(name = "mcc")
    private int mcc;
    @ColumnInfo(name = "mnc")
    private int mnc;
    @ColumnInfo(name = "area")
    private int area;
    @ColumnInfo(name = "radio")
    private String radio;
    @ColumnInfo(name = "lon")
    private double lon;
    @ColumnInfo(name = "lat")
    private double lat;
    @ColumnInfo(name = "range")
    private double range;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
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

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
}
