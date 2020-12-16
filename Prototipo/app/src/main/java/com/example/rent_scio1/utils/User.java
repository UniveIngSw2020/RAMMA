package com.example.rent_scio1.utils;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String user_id;
    private String name;
    private String sourname;
    private String email;
    private String born;
    private String phone;
    private String piva;
    private Boolean trader;
    private String shopname;
    private GeoPoint traderposition;
    private List<GeoPoint> delimited_area=null;

    private List<String> tokens;

    public User(){}


    public User(String user_id, String name, String sourname, String email, String date, String phone, String piva, Boolean trader, String shopname, GeoPoint traderposition, List<GeoPoint> delimited_area, List<String> tokens) {

        this.user_id = user_id;
        this.name = name;
        this.sourname = sourname;
        this.email = email;
        this.born = date;
        this.phone = phone;
        this.piva = piva;
        this.trader = trader;
        this.shopname = shopname;
        this.traderposition = traderposition;
        this.delimited_area = delimited_area;
        this.tokens = tokens;
    }

    public User(User o){
        this.user_id = o.user_id;
        this.name = o.name;
        this.sourname = o.sourname;
        this.email = o.email;
        this.born = o.born;
        this.phone = o.phone;
        this.trader = o.trader;
        this.shopname = o.shopname;
        this.piva = o.piva;
        this.traderposition = o.traderposition;
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

    public String getSourname() {
        return sourname;
    }

    public void setSourname(String sourname) {
        this.sourname = sourname;
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

    public String getPiva() {
        return piva;
    }

    public void setPiva(String piva) {
        this.piva = piva;
    }

    public void setTrader(Boolean trader) {
        this.trader = trader;
    }

    public String getBorn() {
        return born;
    }

    public void setBorn(String born) {
        this.born = born;
    }

    public GeoPoint getTraderposition() {
        return traderposition;
    }

    public void setTraderposition(GeoPoint traderposition) {
        this.traderposition = traderposition;
    }

    public Boolean getTrader() {
        return trader;
    }

    public String getShopname() {
        return shopname;
    }

    public void setShopname(String shopname) {
        this.shopname = shopname;
    }

    public void setTokens(List<String> tokens){
        this.tokens = tokens;
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

    public List<LatLng> getDelimited_areaLatLng(){
        List<LatLng> latLngs = new ArrayList<>();
        for (GeoPoint a: getDelimited_area()) {
            latLngs.add( new LatLng(a.getLatitude(),a.getLongitude()));
        }
        return latLngs;
    }
    public void setDelimited_area(List<GeoPoint> delimited_area) {
        this.delimited_area = delimited_area;
    }


    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", sourname='" + sourname + '\'' +
                ", email='" + email + '\'' +
                ", born='" + born + '\'' +
                ", phone='" + phone + '\'' +
                ", piva='" + piva + '\'' +
                ", trader=" + trader +
                ", shopname='" + shopname + '\'' +
                ", traderposition=" + traderposition + '\'' +
                '}';
    }

}
