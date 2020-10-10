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
        private String cid;
        @ColumnInfo(name = "mcc")
        private String mcc;
        @ColumnInfo(name = "mnc")
        private String mnc;
        @ColumnInfo(name = "area")
        private String area;
        @ColumnInfo(name = "radio")
        private String radio;
        @ColumnInfo(name = "lon")
        private String lon;
        @ColumnInfo(name = "lat")
        private String lat;
        @ColumnInfo(name = "range")
        private String range;

    public cellItem() {
    }

    @NonNull
    public String getCid() {
        return cid;
    }

    public void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    public String getMcc() {
        return mcc;
    }

    public void setMcc(String mcc) {
        this.mcc = mcc;
    }

    public String getMnc() {
        return mnc;
    }

    public void setMnc(String mnc) {
        this.mnc = mnc;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    @Override
    public String toString() {
        return "cellItem{" +
                "cid='" + cid + '\'' +
                ", mcc='" + mcc + '\'' +
                ", mnc='" + mnc + '\'' +
                ", area='" + area + '\'' +
                ", radio='" + radio + '\'' +
                ", lon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                ", range='" + range + '\'' +
                '}';
    }
}
