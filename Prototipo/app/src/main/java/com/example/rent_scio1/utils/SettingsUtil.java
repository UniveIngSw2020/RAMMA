package com.example.rent_scio1.utils;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsUtil {

    public static <T> void updateAttribute(String collectionName, String recordID, String attributeName, T newValue, OnSuccessListener successListener){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection(collectionName).document(recordID);
        mDatabase.update(attributeName, newValue)
                .addOnSuccessListener(successListener);
    }
}
