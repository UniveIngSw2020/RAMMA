package com.example.rent_scio1.utils;

import com.google.firebase.firestore.GeoPoint;

public class Users {
    private String Name;
    private String Sourname;
    private String Email;
    private String Date;
    private String Phone;
    private String Piva;
    private Boolean Trader;
    private GeoPoint Position;

    public Users(String name, String sourname, String email, String date, String phone, String piva, Boolean trader) {
        Name = name;
        Sourname = sourname;
        Email = email;
        Date = date;
        Phone = phone;
        Piva = piva;
        Trader = trader;
    }

    public String getName() {
        return Name;
    }

    /*public void setName(String name) {
        Name = name;
    }*/

    public String getSourname() {
        return Sourname;
    }

    /*public void setSourname(String sourname) {
        Sourname = sourname;
    }*/

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPiva() {
        return Piva;
    }

    /*public void setPiva(String piva) {
        Piva = piva;
    }*/
}
