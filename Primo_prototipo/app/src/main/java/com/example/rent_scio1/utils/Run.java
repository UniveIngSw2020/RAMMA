package com.example.rent_scio1.utils;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Run {
    private GeoPoint geoPoint;
    private @ServerTimestamp Date timestamp;
    private String user;
    private String trader;
    private String vehicle;

    private long startTime;
    private long duration;

    public Run(GeoPoint geoPoint, Date timestamp, String user, String trader, String vehicle,long startTime, long duration) {
        this.geoPoint = geoPoint;
        this.timestamp = timestamp;
        this.user = user;
        this.trader = trader;
        this.vehicle = vehicle;
        this.startTime=startTime;
        this.duration=duration;
    }

    public Run(Run o) {
        this.geoPoint = o.geoPoint;
        this.timestamp = o.timestamp;
        this.user = o.user;
        this.trader = o.trader;
        this.vehicle = o.vehicle;
        this.startTime=o.startTime;
        this.duration=o.duration;
    }

    public Run(){

    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTrader() {
        return trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "Run{" +
                "geoPoint=" + geoPoint +
                ", timestamp=" + timestamp +
                ", user='" + user + '\'' +
                ", trader='" + trader + '\'' +
                ", vehicle='" + vehicle + '\'' +
                '}';
    }
}
