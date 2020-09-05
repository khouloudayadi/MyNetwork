package com.example.mynetwork.Model;

public class BaseStation {
    private int mcc;            // Mobile Country Code
    private int mnc;            // Mobile Network Code
    private int lac;            // Location Area Code or TAC(Tracking Area Code) for LTE
    private int cid;            // Cell Identity
    private double lon;         // Base station longitude
    private double lat;         // Base station latitude
    private int rssi;           // Signal strength
    private String type;        // Signal type, GSM or WCDMA or LTE or CDMA


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
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

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        return "BaseStation {\n" +
                " mcc=" + mcc +
                ",\n mnc=" + mnc +
                ",\n lac=" + lac +
                ",\n cid=" + cid +
                ",\n lon=" + lon +
                ",\n lat=" + lat +
                ",\n RSSI=" + rssi +
                ",\n type='" + type + '\'' +
                "\n }";
    }
}
