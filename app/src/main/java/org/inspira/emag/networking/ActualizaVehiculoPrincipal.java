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
 * Created by jcapiz on 11/03/16.
 */
public class ActualizaVehiculoPrincipal extends Thread {

    private String email;
    private String vehiculo;
    private ObtencionDeAutos.AccionesObtencionDeConvocatorias acciones;

    public ActualizaVehiculoPrincipal(String email, String vehiculo) {
        this.email = email;
        this.vehiculo = vehiculo;
    }

    public void setAcciones(ObtencionDeAutos.AccionesObtencionDeConvocatorias acciones) {
        this.acciones = acciones;
    }

    @Override
    public void run() {
        try {
            JSONObject json = new JSONObject();
            json.put("action", 12);
            json.put("email", email);
            json.put("nombre", vehiculo);
            Log.d("Restructer", json.toString());
            HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
            con.setDoOutput(true);
            DataOutputStream salida = new DataOutputStream(con.getOutputStream());
            salida.write(json.toString().getBytes());
            salida.flush();
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            while ((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            Log.d("CHEWBACCA", "Let's say something puto: " + URLDecoder.decode(baos.toString(), "utf8"));
            json = new JSONObject(URLDecoder.decode(baos.toString(), "utf8"));
            baos.close();
            if(json.getBoolean("content"))
                acciones.obtencionIncorrecta(json.getString("mensaje"));
            else
                acciones.obtencionCorrecta(json);
            con.disconnect();
            entrada.close();
            salida.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
