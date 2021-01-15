package com.example.rent_scio1.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// classe di utilità rappresentante un oggetto utente.

public class User {

    private String user_id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private Boolean trader;
    private String shopName;
    private GeoPoint traderPosition;
    private List<GeoPoint> delimited_area=null;

    private List<String> tokens;

    public User(){}

    public User(String user_id, String name, String surname, String email, String phone, Boolean trader, String shopName, GeoPoint traderPosition, List<GeoPoint> delimited_area, List<String> tokens) {

        this.user_id = user_id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phone = phone;
        this.trader = trader;
        this.shopName = shopName;
        this.traderPosition = traderPosition;
        this.delimited_area = delimited_area;
        this.tokens = tokens;
    }


    public User(User o){
        this.user_id = o.user_id;
        this.name = o.name;
        this.surname = o.surname;
        this.email = o.email;
        this.phone = o.phone;
        this.trader = o.trader;
        this.shopName = o.shopName;
        this.traderPosition = o.traderPosition;
        this.delimited_area = o.delimited_area;
        this.tokens = o.tokens;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setTrader(Boolean trader) {
        this.trader = trader;
    }

    public GeoPoint getTraderPosition() {
        return traderPosition;
    }

    public void setTraderPosition(GeoPoint traderPosition) {
        this.traderPosition = traderPosition;
    }

    public Boolean getTrader() {
        return trader;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public List<String> getTokens(){
        return this.tokens;
    }

    public boolean addToken(String s){
        if(this.tokens == null) this.tokens = new ArrayList<>();
        if(!this.tokens.contains(s)) return this.tokens.add(s);
        return false;
    }

    public List<GeoPoint> getDelimited_area() {
        return delimited_area;
    }

    public List<LatLng> convertDelimited_areaLatLng(){
        if(delimited_area!=null){
            List<LatLng> latLngs = new ArrayList<>();
            for (GeoPoint a: getDelimited_area()) {
                latLngs.add( new LatLng(a.getLatitude(),a.getLongitude()));
            }
            return latLngs;
        }
        else{
            return null;
        }
    }

    public void setDelimited_area(List<GeoPoint> delimited_area) {
        this.delimited_area = delimited_area;
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", trader=" + trader +
                ", shopName='" + shopName + '\'' +
                ", traderPosition=" + traderPosition + '\'' +
                '}';
    }
}
