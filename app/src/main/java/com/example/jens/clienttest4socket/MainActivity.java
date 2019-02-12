package com.example.jens.clienttest4socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends Activity {

    TextView textResponse, textResponse2;
    TextView tourKmResponse, durchschnittsKmhResponse, hoechstkmhResponse;
    Button tourStartButton, tourRessetButton;
    //gesendete Daten und diese Daten umgewandelt in Int
    String a, b;
    double kmh, km;


    //Variablen für Berechnung der Durchschnittskmh
    double kmhSumme = 0, durchschnittsKmh = 0;
    int kmhSummeCount=0;
    //Variablen für die Tourkilometer
    double tourStartKilometer;
    double tourKilometer;

    //Variablen für die höchstkmh
    double hoechstKmh=0;

    MyClientTask myClientTask;
    boolean tourRunning = false;


    //Handler der mit der Funktion postDelayed die Dauer zwischen dem Senden der Daten einstellt (in ms)
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getData();
            handler.postDelayed(this, 1000);
        }
    };


    //Selbst definierte Funktion die ein neues Senden der Daten auslöst
    // STELLT BEIM START DER APP EINE VERBINDUNG HER. SIND DIE GERÄTE NICHT IM SELBEN NETZWERK, SO FÄNGT DAS TRY/CATCH DEN ABSTURZ AB
    public void getData() {


        try {
            MyClientTask myClientTask = new MyClientTask("192.168.4.1", 8080);
            myClientTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.jens.clienttest4socket", Context.MODE_PRIVATE);





        //TEXTRESPONSE GIBT DEN GESENDETEN TEXT AUS
        //response = kmh
        textResponse = (TextView) findViewById(R.id.response);
        //response2 = km
        textResponse2 = (TextView) findViewById(R.id.response2);
        tourStartButton = (Button) findViewById(R.id.tourStart);
        tourRessetButton = (Button) findViewById(R.id.tourResset);
        tourKmResponse = (TextView) findViewById(R.id.tourKM);
        durchschnittsKmhResponse = (TextView) findViewById(R.id.durchschnittskmh);
        hoechstkmhResponse = (TextView) findViewById(R.id.hoechstkmh);


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (sharedPreferences.getBoolean("tourRunning",false)) {

            tourStartButton.setEnabled(false);
            tourRessetButton.setEnabled(true);

            tourKmResponse.setText("0.0");
            durchschnittsKmhResponse.setText("0.0");
            hoechstkmhResponse.setText("0.0");

        }else{

            tourStartButton.setEnabled(true);
            tourRessetButton.setEnabled(false);

            tourKmResponse.setText("TourKM");
            durchschnittsKmhResponse.setText("Durchschnittskm/h");
            hoechstkmhResponse.setText("Hoechstkm/h");



        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        /*
        tourStartButton.setEnabled(true);
        tourRessetButton.setEnabled(false);
*/



        //WENN BEIM START KEINE VERBINDUNG MÖGLICH WAR, SO KANN MAN MIT DIESEM KNOPF EINE VERBINDUNG HERSTELLEN
        tourStartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tourRunning = true;


                /*
                //HIERFÜR ETWAS ANDERES AUSDENKEN!
                try {
                    tourStartKilometer = Double.parseDouble(textResponse2.getText().toString());
                }catch(Exception e){
                    e.printStackTrace();
                }
*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                sharedPreferences.edit().putString("tourStartKilometer",b).apply(); //tourstartkilometer als string! gespeichert
                Toast.makeText(getBaseContext(),sharedPreferences.getString("tourStartKilometer","0"),Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putString("hoechstKmh",a).apply(); //die ersten kmh als hoechstkmh speichern
                sharedPreferences.edit().putString("kmhSummeDurch",a).apply(); //das erste a ist die erste kmh Summe
                sharedPreferences.edit().putInt("kmhSummeCount",1).apply();
                sharedPreferences.edit().putBoolean("tourRunning",true).apply();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////



                tourKmResponse.setText("0.0");
                durchschnittsKmhResponse.setText("0.0");
                hoechstkmhResponse.setText("0.0");




                tourStartButton.setEnabled(false);
                tourRessetButton.setEnabled(true);
            }
        });

        tourRessetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tourRunning = false;


                /////////////////////////////////////////////
                sharedPreferences.edit().clear().commit();
                sharedPreferences.edit().putBoolean("tourRunning",false).apply();
                ////////////////////////////////////////////


                kmhSumme = 0;
                kmhSummeCount = 0;
                durchschnittsKmh = 0;

                tourStartKilometer = 0;
                tourKilometer = 0;

                hoechstKmh = 0;

                tourKmResponse.setText("TourKM");
                durchschnittsKmhResponse.setText("Durchschnittskm/h");
                hoechstkmhResponse.setText("Hoechstkm/h");


                tourStartButton.setEnabled(true);
                tourRessetButton.setEnabled(false);
            }
        });

        //Der Handler, welcher die Verbindung herstellt und beim Start der App eine erste Verbidung aufbaut (Wofür stehen die 1000ms? ggf. für den Delay vor dem Start?)
        handler.postDelayed(runnable, 1000);


    }

    //Asynctask, welche für das Verarbeiten der gesendeten Daten verantwortlich ist
    public class MyClientTask extends AsyncTask<Void, Void, Void> {

        String dstAddress;
        int dstPort;
        String response;

        TourFuncs tourFuncs = new TourFuncs();

        DecimalFormat formatter = new DecimalFormat("#.##");

        SharedPreferences sharedPreferences = getSharedPreferences("com.example.jens.clienttest4socket", MODE_PRIVATE);


        MyClientTask(String addr, int port) {
            dstAddress = addr;
            dstPort = port;
        }


        @Override
        protected Void doInBackground(Void... arg0) {

            try {
                Socket socket = new Socket(dstAddress, dstPort);
                InputStream inputStream = socket.getInputStream();
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];


                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }


                socket.close();
                response = byteArrayOutputStream.toString("UTF-8");


                //Zerlegt den übertragenden String in die beiden zu übertragenden Daten, die Daten werden durch "#" und "*" getrennt (Auf Serverseite)
                a = response.substring(0, response.indexOf("#"));
                b = response.substring(response.indexOf("#") + 1, response.indexOf("*"));


                try
                {
                    kmh = Double.parseDouble(a);
                    km = Double.parseDouble(b);

                }
                catch(NumberFormatException n)
                {
                    n.printStackTrace();
                }

/*
                if (tourRunning) {
                    //durchschnittskmh berechnung
                    kmhSumme = kmhSumme + kmh;
                    kmhSummeCount++;
                    durchschnittsKmh = kmhSumme/kmhSummeCount;

                    //tourkm berechnung
                    tourKilometer=tourFuncs.tourKilometer(km,tourStartKilometer);

                    //hoechstgeschwindigeit berechnen
                    hoechstKmh = tourFuncs.höchstgeschwindigkeit(kmh,hoechstKmh);


                }
*/
///////////////////////////////////////////////////////////////
                if (sharedPreferences.getBoolean("tourRunning",true)) {

                    //durchschnittskmh

                    kmhSumme = Double.parseDouble(sharedPreferences.getString("kmhSummeDurch","0")) + kmh;
                    sharedPreferences.edit().putString("kmhSummeDurch",Double.toString(kmhSumme)).apply();

                    kmhSummeCount = sharedPreferences.getInt("kmhSummeCount",0) + 1;
                    sharedPreferences.edit().putInt("kmhSummeCount",kmhSummeCount).apply();

                    durchschnittsKmh = Double.parseDouble(sharedPreferences.getString("kmhSummeDurch","0"))/sharedPreferences.getInt("kmhSummeCount",0);
                    //tourkm


                    tourKilometer = tourFuncs.tourKilometer(km,Double.parseDouble(sharedPreferences.getString("tourStartKilometer","0").replace(",",".")));

                    //hoechstgesch

                    hoechstKmh = tourFuncs.höchstgeschwindigkeit(kmh,Double.parseDouble(sharedPreferences.getString("hoechstKmh","0")));

                    if(hoechstKmh>60){
                        hoechstKmh=60;
                    }

                    sharedPreferences.edit().putString("hoechstKmh",Double.toString(hoechstKmh)).apply();

                }


                //////////////////////////////////////////////////



            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;

        }

        //onPostExecute wird nach dem übertragen ausgeführt? Hier werden zum Beispiel die Texte der TextViews geändert (Oder auch bsp. Shared preferences verwendet?))
        @Override
        protected void onPostExecute(Void result) {


            //textResponse.setText(response);

            textResponse.setText(a);
            textResponse2.setText(b);
            super.onPostExecute(result);

            if (sharedPreferences.getBoolean("tourRunning",false)) {
                //tourKmResponse.setText("Testdaten1: "+a+4);
                tourKmResponse.setText("Tkm: "+tourKilometer);
                durchschnittsKmhResponse.setText("Dkm/h: " +durchschnittsKmh);
                hoechstkmhResponse.setText("Hkm/h: "+hoechstKmh);
            }


        }
    }
}