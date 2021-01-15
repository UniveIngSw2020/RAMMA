package com.example.rent_scio1.utils.Settings;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

// classe di utilità per impostazioni che permette di caricare su DB un valore variabile e di impostare un OnSuccessListener. Utilizzato in SettingsActivityTextView.

public class SettingsUtil {

    public static void updateAttribute(String collectionName, String recordID, String attributeName, String newValue, OnSuccessListener successListener){
        DocumentReference mDatabase = FirebaseFirestore.getInstance().collection(collectionName).document(recordID);
        mDatabase.update(attributeName, newValue)
                .addOnSuccessListener(successListener);
    }
}
