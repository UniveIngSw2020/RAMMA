package com.example.rent_scio1.Trader.TutorialDelimitedArea;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rent_scio1.R;

//Fragment di delimitazione interfaccia grafica tutorial area limitata.

public class ConfirmFragmentTutorial extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public ConfirmFragmentTutorial() {

    }

    public static ConfirmFragmentTutorial newInstance(String param1, String param2) {
        ConfirmFragmentTutorial fragment = new ConfirmFragmentTutorial();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_conferma_tutorial, container, false);
    }
}