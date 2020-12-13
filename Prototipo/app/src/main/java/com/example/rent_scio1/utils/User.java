package com.example.rent_scio1.utils;


import com.google.firebase.firestore.GeoPoint;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String user_id;
    private String name;
    private String surname;
    private String email;
    private String phone;
    private Boolean trader;
    private String shopname;
    private GeoPoint traderposition;
    private List<GeoPoint> delimited_area=null;

    public User(){}


    public User(String user_id, String name, String sourname, String email, String phone, Boolean trader, String shopname, GeoPoint traderposition, List<GeoPoint> delimited_area) {

        this.user_id = user_id;
        this.name = name;
        this.surname = sourname;
        this.email = email;
        this.phone = phone;
        this.trader = trader;
        this.shopname = shopname;
        this.traderposition = traderposition;
        this.delimited_area=delimited_area;
    }

    public User(User o){
        this.user_id = o.user_id;
        this.name = o.name;
        this.surname = o.surname;
        this.email = o.email;
        this.phone = o.phone;
        this.trader = o.trader;
        this.shopname = o.shopname;
        this.traderposition = o.traderposition;
        this.delimited_area=o.delimited_area;
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
        return surname;
    }

    public void setSourname(String sourname) {
        this.surname = sourname;
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

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", sourname='" + surname + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", trader=" + trader +
                ", shopname='" + shopname + '\'' +
                ", traderposition=" + traderposition + '\'' +
                '}';
    }

    public List<GeoPoint> getDelimited_area() {
        return delimited_area;
    }

    public void setDelimited_area(List<GeoPoint> delimited_area) {

        this.delimited_area = delimited_area;
    }
}
