package com.example.rent_scio1.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class MyMap implements OnMapReadyCallback, Parcelable {

    private static GoogleMap mMap;

    protected MyMap(Parcel in) { }

    public static final Creator<MyMap> CREATOR = new Creator<MyMap>() {
        @Override
        public MyMap createFromParcel(Parcel in) {
            return new MyMap(in);
        }

        @Override
        public MyMap[] newArray(int size) {
            return new MyMap[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
