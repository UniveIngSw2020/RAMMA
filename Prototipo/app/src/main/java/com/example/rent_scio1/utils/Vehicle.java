package com.example.rent_scio1.utils;

import androidx.annotation.NonNull;

public class Vehicle {

    //campo per visualizzazione massima veicoli
    public static final int maxVehicles=20;

    //tipo veicolo, posti, ID , noleggiato(bool)
    private String vehicleType;
    private String fk_trader;
    private String vehicleUID;
    private int seats;
    private boolean rented;
    private double maxSpeedKMH;


    public Vehicle(Vehicle v) {
        this.vehicleType = v.vehicleType;
        this.fk_trader = v.fk_trader;
        this.seats = v.seats;
        this.rented = v.rented;
        this.vehicleUID = v.vehicleUID;
        this.maxSpeedKMH=v.maxSpeedKMH;
    }

    public Vehicle(){
        vehicleType = null;
        seats = 0;
        fk_trader = null;
        rented = false;
        vehicleUID = null;
        maxSpeedKMH=0;
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

    public boolean isRented() {
        return rented;
    }

    public void setRented(boolean rented) {
        this.rented = rented;
    }

    public String getFk_trader() { return fk_trader; }

    public void setFk_trader(String fk_trader) { this.fk_trader = fk_trader; }

    public String getVehicleUID() { return vehicleUID; }

    public void setVehicleUID(String vehicleUID) { this.vehicleUID = vehicleUID; }

    public static int getMaxVehicles() {
        return maxVehicles;
    }

    public double getMaxSpeedKMH() {
        return maxSpeedKMH;
    }

    public void setMaxSpeedKMH(double maxSpeedKMH) {
        this.maxSpeedKMH = maxSpeedKMH;
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
