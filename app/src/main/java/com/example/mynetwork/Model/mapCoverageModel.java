package com.example.mynetwork.Model;

public class mapCoverageModel implements Comparable<mapCoverageModel>{
    private String radio;
    private int cid;
    private int area;
    private int range;
    private double lat;
    private double lon;
    private double distanceUser;

    public mapCoverageModel(String radio, int cid, int area, int range, double lat, double lon, double distanceUser) {
        this.radio = radio;
        this.cid = cid;
        this.area = area;
        this.range = range;
        this.lat = lat;
        this.lon = lon;
        this.distanceUser = distanceUser;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getDistanceUser() {
        return distanceUser;
    }

    public void setDistanceUser(double distanceUser) {
        this.distanceUser = distanceUser;
    }

    @Override
    public String toString() {
        return "mapCoverageModel{" +
                "radio='" + radio + '\'' +
                ", cid=" + cid +
                ", area=" + area +
                ", range=" + range +
                ", lat=" + lat +
                ", lon=" + lon +
                ", distanceUser=" + distanceUser +
                '}' + "\n";
    }

    @Override
    public int compareTo(mapCoverageModel o) {
        return Double.compare(distanceUser,o.distanceUser);
    }
}
