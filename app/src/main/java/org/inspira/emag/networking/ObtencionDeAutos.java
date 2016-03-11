package org.inspira.emag.networking;

import android.util.Log;

import org.inspira.emag.actividades.MainActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by jcapiz on 5/03/16.
 */
public class ObtencionDeAutos extends Thread {

    private String email;
    private AccionesObtencionDeConvocatorias acciones;

    public ObtencionDeAutos(String email){
        this.email = email;
    }

    public interface AccionesObtencionDeConvocatorias{
        void obtencionCorrecta(JSONObject json);
        void obtencionIncorrecta(String mensaje);
    }

    public void setAcciones(AccionesObtencionDeConvocatorias acciones) {
        this.acciones = acciones;
    }

    @Override
    public void run(){
        try{
            JSONObject json = new JSONObject();
            json.put("action", 10);
            json.put("email", email);
            HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
            con.setDoOutput(true);
            DataOutputStream salida = new DataOutputStream(con.getOutputStream());
            salida.write(json.toString().getBytes());
            salida.flush();
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            Log.d("CHEWBACCA", URLDecoder.decode(baos.toString(), "utf8"));
            json = new JSONObject(URLDecoder.decode(baos.toString(), "utf8"));
            baos.close();
            try{
                json.getBoolean("content");
                acciones.obtencionIncorrecta(json.getString("mensaje"));
            }catch(JSONException ignore){
                acciones.obtencionCorrecta(json);
            }
            con.disconnect();
            entrada.close();
            salida.close();
        }catch(IOException | JSONException e){
            e.printStackTrace();
        }
    }
}
