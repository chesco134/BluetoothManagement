package org.inspira.emag.networking;

import android.content.Context;
import android.util.Log;

import org.inspira.emag.actividades.MainActivity;
import org.inspira.emag.database.TripsData;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jcapiz on 24/12/15.
 */
public class CommitTrip extends Thread {

    private int idViaje;
    private Context ctx;

    public CommitTrip(Context ctx, int idViaje){
        this.ctx = ctx;
        this.idViaje = idViaje;
    }

    @Override
    public void run(){
        try{
            HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
            con.setDoOutput(true);
            JSONObject json = new JSONObject();
            DataOutputStream salida;
            json.put("action","5");
            json.put("fechaFin", (new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
            json.put("idViaje", idViaje);
            Log.d("EMAG","Sending something:\n" + json.toString());
            salida = new DataOutputStream(con.getOutputStream());
            salida.write(json.toString().getBytes());
            salida.flush();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int length;
            byte[] chunk = new byte[512];
            while((length = entrada.read(chunk))!=-1)
                baos.write(chunk,0,length);
            if(baos.toString().equals("OK")){
                TripsData tripDB = new TripsData(ctx);
                tripDB.terminaFechaTrip(idViaje, new Date());
                }
            baos.close();
        }catch(IOException e){
            Log.d("Error from Commiter", e.getMessage());
        }catch(JSONException e){
            Log.d("JSON Speacker", "From Commiter");
            e.printStackTrace();
        }
    }
}
