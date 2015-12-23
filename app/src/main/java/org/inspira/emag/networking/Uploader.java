package org.inspira.emag.networking;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.inspira.emag.shared.Location;
import org.inspira.emag.shared.RPM;
import org.inspira.emag.shared.RawReading;
import org.inspira.emag.shared.Shareable;
import org.inspira.emag.shared.Speed;
import org.inspira.emag.shared.Trip;

/**
 * Created by jcapiz on 22/12/15.
 */
public class Uploader extends Thread {

    private static final String TAG = "Uploader";

    private Shareable[] readings;

    public Uploader(Shareable... params){
        readings = params;
    }

    @Override
    public void run(){
        Log.d(TAG, "Uploading " + readings.length + " readings..");
        try {
            for (Shareable reading : readings) {
                HttpURLConnection con = (HttpURLConnection) new URL("http://192.168.1.72:8080/HelloWorldWeb/VehicleDataReceiver").openConnection();
                con.setDoOutput(true);
                JSONObject json = new JSONObject();
                DataOutputStream salida = new DataOutputStream(con.getOutputStream());
                if( reading instanceof Location)
                    try {
                        json.put("action","4");
                        json.put("Latitud", ((Location) reading).getLatitud());
                        json.put("Longitud", ((Location) reading).getLongitud());
                        json.put("idViaje", String.valueOf(((Location) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if(reading instanceof RPM)
                    try {
                        json.put("action","2");
                        json.put("RPM", ((RPM) reading).getRpmValue());
                        json.put("idViaje", String.valueOf(((RPM) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if( reading instanceof Speed )
                    try {
                        json.put("action","3");
                        json.put("SPEED", ((Speed) reading).getSpeed());
                        json.put("idViaje", String.valueOf(((Speed) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if(reading instanceof Trip )
                    try {
                        json.put("action","1");
                        json.put("idViaje", String.valueOf(((Trip) reading).getIdTrip()));
                        json.put("fechaInicio", (((Trip) reading).getFechaInicio()));
                        try {
                            json.put("fechaFin", (((Trip) reading).getFechaFin()));
                        }catch(NullPointerException e){
                            //Log.d("Tulman",e.getMessage());
                        }
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if(reading instanceof RawReading)
                    json = ((RawReading)reading).getJson();
                Log.d("EMAG","Sending something:\n" + json.toString());
                salida.write(json.toString().getBytes());
                salida.flush();
                DataInputStream entrada = new DataInputStream(con.getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int length;
                byte[] chunk = new byte[512];
                while((length = entrada.read(chunk))!=-1)
                    baos.write(chunk,0,length);
                if(baos.toString().equals("OK") && ! (reading instanceof Trip) ) {
//                    readings[0].commitEntry(MyLocationProvider.this);
                    Log.d("Kivine Maa", "Commiting something // " + baos.toString());
                }
                salida.close();
                con.disconnect();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        Log.d(TAG, "Done");
    }

    private String grabServerURL(){
        String url = "189.232.93.67";
        try{
            URL serverURL = new URL("http://votacionesipn.com/services/?tag=gimmeAddr");
            HttpURLConnection con = (HttpURLConnection)serverURL.openConnection();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            byte[] bytesChunk = new byte[512];
            int bytesLeidos;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((bytesLeidos = entrada.read(bytesChunk))!=-1)
                baos.write(bytesChunk, 0, bytesLeidos);
            JSONObject json = new JSONObject(baos.toString());
            con.disconnect();
            url = json.getString("content");
        }catch(IOException e){
            e.printStackTrace();
            Log.e("GrabServerURL", e.getMessage());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("GSURL JSON", e.getMessage());
        }
        return url;
    }
}
