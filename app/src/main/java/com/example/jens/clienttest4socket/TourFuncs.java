package com.example.jens.clienttest4socket;

import android.content.SharedPreferences;

public class TourFuncs {



    public int höchstgeschwindigkeit(int kmhaktuell, int hoechstkmh){

        int returnNeueHöchstKmh;

        if(kmhaktuell > hoechstkmh){
            returnNeueHöchstKmh = kmhaktuell;
            return returnNeueHöchstKmh;
        }else{

            returnNeueHöchstKmh = hoechstkmh;

            return returnNeueHöchstKmh;
        }
    }

    //Datentypen noch nicht ganz klar;
    public int tourKilometer(int aktuellekilometer, int tourStartKilometer){

        int tourkm;

        tourkm = aktuellekilometer-tourStartKilometer;

        return tourkm;
    }

}
