package com.example.jens.clienttest4socket;

import android.content.SharedPreferences;

public class TourFuncs {



    public double höchstgeschwindigkeit(double kmhaktuell, double hoechstkmh){

        double returnNeueHöchstKmh;

        if(kmhaktuell > hoechstkmh){
            returnNeueHöchstKmh = kmhaktuell;
            return returnNeueHöchstKmh;
        }else{

            returnNeueHöchstKmh = hoechstkmh;

            return returnNeueHöchstKmh;
        }
    }

    //Datentypen noch nicht ganz klar;
    public double tourKilometer(double aktuellekilometer, double tourStartKilometer){

        double tourkm;

        tourkm = aktuellekilometer-tourStartKilometer;

        return tourkm;
    }

}
