package org.inspira.emag.networking;

import android.app.Activity;
import android.util.Log;

import org.inspira.emag.actividades.MainActivity;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.dialogos.ProveedorToast;
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

/**
 * Created by jcapiz on 1/03/16.
 */
public class AltaVehiculo extends Thread {

    private String texto;
    private Activity context;

    public AltaVehiculo(Activity context, String texto){
        this.texto = texto;
        this.context = context;
    }

    @Override
    public void run(){
        try {
            JSONObject json = new JSONObject();
            json.put("action", 8); // Solicitud de agregar un nuevo vehículo
            TripsData db = new TripsData(context);
            User user = db.getUserData();
            json.put("email", user.getEmail());
            json.put("vehiculo", texto);
            HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
            con.setDoOutput(true);
            DataOutputStream salida = new DataOutputStream(con.getOutputStream());
            salida.write(json.toString().getBytes());
            salida.flush();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((length = entrada.read(chunk)) != -1)
                baos.write(chunk, 0, length);
            Log.d("Momonga", baos.toString());
            json = new JSONObject(baos.toString());
            baos.close();
            if (json.getBoolean("content")) {
                Vehiculo vehiculo = new Vehiculo();
                vehiculo.setEmail(user.getEmail());
                vehiculo.setNombre(texto);
                if(new TripsData(context).addVehiculo(vehiculo))
                    ProveedorToast
                            .showToastOnUIThread(context, "Cambio realizado con éxito");
                else
                    ProveedorToast.showToastOnUIThread(context, "El vehículo ya existe");
            } else {
                ProveedorToast.showToastOnUIThread(context, "Por favor revise la información");
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            ProveedorToast.showToastOnUIThread(context, "Servicio temporalmente no disponible");
        }
    }
}
