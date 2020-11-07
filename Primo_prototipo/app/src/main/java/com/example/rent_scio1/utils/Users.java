package com.example.rent_scio1.utils;

import com.google.firebase.firestore.GeoPoint;

public class Users {
    private String user_id;
    private String name;
    private String sourname;
    private String email;
    private String date;
    private String phone;
    private String piva;
    private Boolean trader;
    private GeoPoint Position;

    public Users(){}

    public Users(String user_id, String name, String sourname, String email, String date, String phone, String piva, Boolean trader) {
        this.user_id = user_id;
        this.name = name;
        this.sourname = sourname;
        this.email = email;
        this.date = date;
        this.phone = phone;
        this.piva = piva;
        this.trader = trader;
    }
    public Users(Users o){
        this.user_id = o.user_id;
        this.name = o.name;
        this.sourname = o.sourname;
        this.email = o.email;
        this.date = o.date;
        this.phone = o.phone;
        this.piva = o.piva;
        this.trader = o.trader;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public GeoPoint getPosition() {
        return Position;
    }

    public void setPosition(GeoPoint position) {
        Position = position;
    }

    public boolean getTrader (){
        return trader;
    }

    @Override
    public String toString() {
        return "Users{" +
                "user_id='" + user_id + '\'' +
                ", name='" + name + '\'' +
                ", sourname='" + sourname + '\'' +
                ", email='" + email + '\'' +
                ", date='" + date + '\'' +
                ", phone='" + phone + '\'' +
                ", piva='" + piva + '\'' +
                ", trader=" + trader +
                ", Position=" + Position +
                '}';
    }
}
