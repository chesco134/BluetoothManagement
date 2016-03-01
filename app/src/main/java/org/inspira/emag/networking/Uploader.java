package org.inspira.emag.networking;

import android.content.Context;
import android.util.Log;

import org.inspira.emag.actividades.MainActivity;
import org.inspira.emag.actividades.OrganizarVehiculos;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.gps.MyLocationProvider;
import org.inspira.emag.shared.User;
import org.inspira.emag.shared.Vehiculo;
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
    private Context ctx;

    public Uploader(Shareable... params){
        readings = params;
    }

    public void setContext(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void run(){
        Log.d(TAG, "Uploading " + readings.length + " readings..");
        try {
            for (Shareable reading : readings) {
                HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
                con.setDoOutput(true);
                JSONObject json = new JSONObject();
                DataOutputStream salida = new DataOutputStream(con.getOutputStream());
                if( reading instanceof Location)
                    try {
                        json.put("action",4);
                        json.put("Latitud", ((Location) reading).getLatitud());
                        json.put("Longitud", ((Location) reading).getLongitud());
                        json.put("idViaje", String.valueOf(((Location) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if(reading instanceof RPM)
                    try {
                        json.put("action",2);
                        json.put("RPM", ((RPM) reading).getRpmValue());
                        json.put("idViaje", String.valueOf(((RPM) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if( reading instanceof Speed )
                    try {
                        json.put("action",3);
                        json.put("SPEED", ((Speed) reading).getSpeed());
                        json.put("idViaje", String.valueOf(((Speed) reading).getIdTrip()));
                    } catch (JSONException re) {
                        Log.e(TAG, re.toString());
                    }
                else if(reading instanceof Trip )
                    try {
                        json.put("action",1);
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
                try {
                    json.put("email", new TripsData(ctx).getUserData().getEmail());
                    json.put("vehiculo", ctx.getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).getString("vehiculo", null));
                }catch(JSONException ignore){}
                Log.d("EMAG","Sending something:\n" + json.toString());
                salida.write(json.toString().getBytes());
                salida.flush();
                DataInputStream entrada = new DataInputStream(con.getInputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int length;
                byte[] chunk = new byte[512];
                while((length = entrada.read(chunk))!=-1)
                    baos.write(chunk,0,length);
                Log.d("Mommonga", baos.toString());
                if(baos.toString().equals("OK") && ! (reading instanceof Trip || reading instanceof User || reading instanceof Vehiculo) ) {
                    reading.commitEntry(ctx);
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
}
