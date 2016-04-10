package org.inspira.emag.networking;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.inspira.emag.actividades.MainActivity;
import org.inspira.emag.actividades.OrganizarVehiculos;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.shared.RawReading;
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
    private int idVehiculo;
    private Context context;
    private ObtencionDeAutos.AccionesObtencionDeConvocatorias acciones;

    public ActualizaVehiculoPrincipal(Context context, String email, int idVehiculo) {
        this.email = email;
        this.idVehiculo = idVehiculo;
        this.context = context;
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
            json.put("idVehiculo", idVehiculo);
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
            Log.d("CHEWBACCA", "Let's say something: " + URLDecoder.decode(baos.toString(), "utf8"));
            RawReading raw = new RawReading("Let's say something: " + URLDecoder.decode(baos.toString(), "utf8"));
            new Uploader(raw).start();
            json = new JSONObject(URLDecoder.decode(baos.toString(), "utf8"));
            baos.close();
            if(!json.getBoolean("content"))
                acciones.obtencionIncorrecta(json.getString("mensaje"));
            else {
                acciones.obtencionCorrecta(json);
                TripsData database = new TripsData(context);
                SQLiteDatabase db = database.getReadableDatabase();
                Cursor c = db.rawQuery("select nombre from Vehiculo where idVehiculo = " +
                        "CAST(? as INTEGER)", new String[]{String.valueOf(idVehiculo)});
                c.moveToFirst();
                final String vehiculo = c.getString(0);
                c.close();
                db.close();
                ((AppCompatActivity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((OrganizarVehiculos) context).actualizarEtiquetaVehiculoPrincipal(vehiculo);
                    }
                });
            }
            con.disconnect();
            entrada.close();
            salida.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
