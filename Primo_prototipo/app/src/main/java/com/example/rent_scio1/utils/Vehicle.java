package com.example.rent_scio1.utils;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Vehicle {

    //campo per visualizzazione massima veicoli
    public static final int maxVehicles=10;

    //tipo veicolo, posti, ID , noleggiato(bool)
    private String vehicleType;
    private int seats;
    private int ID;
    private boolean rented;

    public Vehicle(Vehicle v) {
        this.vehicleType = v.vehicleType;
        this.seats = v.seats;
        this.ID = v.ID;
        this.rented = v.rented;
    }

    public Vehicle(String vehicleType, int seats, int ID, boolean rented) {
        this.vehicleType = vehicleType;
        this.seats = seats;
        this.ID = ID;
        this.rented = rented;
    }

    public Vehicle(){
        vehicleType=null;
        seats=0;
        ID=0;
        rented=false;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public boolean isRented() {
        return rented;
    }

    public void setRented(boolean rented) {
        this.rented = rented;
    }

    @NonNull
    @Override
    public String toString() {

        if(vehicleType==null){
            return "Seleziona un veicolo: ";
        }
        else{
            return  (this.getSeats()==1) ? String.format("%s (%d posto)",this.getVehicleType(),this.getSeats()) : String.format("%s (%d posti)",this.getVehicleType(),this.getSeats());
        }

    }

}
