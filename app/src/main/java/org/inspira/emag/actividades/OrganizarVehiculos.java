package org.inspira.emag.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.capiz.bluetooth.R;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.dialogos.DialogoDeConsultaSimple;
import org.inspira.emag.dialogos.ObtenerTexto;
import org.inspira.emag.dialogos.ProveedorSnackBar;
import org.inspira.emag.dialogos.RemueveElementosDeLista;
import org.inspira.emag.networking.AltaVehiculo;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jcapiz on 29/02/16.
 */
public class OrganizarVehiculos extends AppCompatActivity {

    private ListView lista;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perfiles_de_autos);
        lista = (ListView) findViewById(R.id.perfiles_de_autos_lista_de_perfiles);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchDialogoInformacion(((TextView) view).getText().toString());
            }

            private void launchDialogoInformacion(final String nombreDeVehiculo) {
                DialogoDeConsultaSimple info = new DialogoDeConsultaSimple();
                Bundle args = new Bundle();
                args.putString("mensaje", "¿Quiere seleccionar el vehículo como predeterminado?");
                info.setArguments(args);
                info.setAgenteDeInteraccion(new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
                    @Override
                    public void clickSobreAccionPositiva(DialogFragment dialogo) {
                        updateVehiculo(nombreDeVehiculo);
                    }

                    @Override
                    public void clickSobreAccionNegativa(DialogFragment dialogo) {
                    }
                });
                info.show(getSupportFragmentManager(), "Shark");
            }
        });
        String nombreVehiculo = "Auto actual: " + getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).getString("vehiculo", "NaN");
        ((TextView)findViewById(R.id.perfiles_de_autos_auto_actual)).setText(nombreVehiculo);
        Vehiculo[] vehiculos = new TripsData(this).obtenerVehiculosValidos();
        List<String> nombres = new ArrayList<>();
        for(Vehiculo v : vehiculos)
            nombres.add(v.getNombre());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        lista.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.configuracion_de_perfiles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        boolean resolved = false;
        if( id == R.id.add ){
            launchDialogoAgregar();
            resolved = true;
        }else if( id == R.id.remove ){
            launchDialogoRemoverElementos();
            resolved =  true;
        }
        return resolved;
    }

    private void updateVehiculo(String nombreDeVehiculo){
        SharedPreferences.Editor editor = getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putString("vehiculo", nombreDeVehiculo);
        editor.apply();
        String nombreVehiculo = "Auto actual: " + nombreDeVehiculo;
        ((TextView)findViewById(R.id.perfiles_de_autos_auto_actual)).setText(nombreVehiculo);
        ProveedorSnackBar
                .muestraBarraDeBocados(lista, "Hecho");
    }

    private void launchDialogoRemoverElementos() {
        RemueveElementosDeLista rm = new RemueveElementosDeLista();
        Bundle args = new Bundle();
        final List<String> elementos = new ArrayList<>();
        String vActual = getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).getString("vehiculo", "NaN");
        for(Vehiculo v : new TripsData(this).obtenerVehiculosValidos())
            if(!vActual.equals(v.getNombre())) elementos.add(v.getNombre());
        args.putStringArray("elementos", elementos.toArray(new String[0]));
        rm.setArguments(args);
        rm.setAd(new RemueveElementosDeLista.AccionDialogo() {
            @Override
            public void accionPositiva(DialogFragment fragment) {
                Integer[] indices = ((RemueveElementosDeLista) fragment).getElementosSeleccionados();
                prepareElements(indices);
                final List<String> sublist = new ArrayList<>();
                for(Integer index : indices){
                    sublist.add(elementos.get(index));
                }
                final TripsData db = new TripsData(OrganizarVehiculos.this);
                db.colocarVehiculosEnNoBorrado(sublist.toArray(new String[0]));
                new Thread(){
                    @Override
                    public void run(){
                        for( String nombre : sublist )
                        try{
                            JSONObject json = new JSONObject();
                            json.put("action", 9); // Solicitud de borrar vehículo para el usuario.
                            json.put("nombre", nombre);
                            json.put("email", db.getUserData().getEmail());
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
                            Log.d("Momonga", baos.toString());
                            json = new JSONObject(baos.toString());
                            baos.close();
                            if(json.getBoolean("content")){
                                db.removerVehiculo(nombre);
                                runOnUiThread(new Poster(nombre));
                            }
                        }catch(JSONException | IOException e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }

            @Override
            public void accionNegativa(DialogFragment fragment) {}
        });
        rm.show(getSupportFragmentManager(), "Remover elementos");
    }

    private void launchDialogoAgregar() {
        ObtenerTexto ot = new ObtenerTexto();
        Bundle args = new Bundle();
        args.putString("mensaje", "Escriba el nombre de su auto");
        ot.setArguments(args);
        ot.setAgenteDeInteraccion(new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
            @Override
            public void clickSobreAccionPositiva(DialogFragment dialogo) {
                ObtenerTexto ot = (ObtenerTexto) dialogo;
                final String texto = ot.obtenerTexto();
                adapter.add(texto);
                new AltaVehiculo(OrganizarVehiculos.this, texto).start();
            }

            @Override
            public void clickSobreAccionNegativa(DialogFragment dialogo) {
            }
        });
        ot.show(getSupportFragmentManager(), "Agregar Carro");
    }

    private void prepareElements(Integer[] elements){
        Integer hold;
        for(int i=0; i<elements.length; i++){
            for(int j=i+1; j<elements.length; j++){
                if(elements[i] < elements[j]){
                    hold = elements[i];
                    elements[i] = elements[j];
                    elements[j] = hold;
                }
            }
        }
    }

    private class Poster implements Runnable{

        private String nombre;

        public Poster(String nombre){
            this.nombre = nombre;
        }

        @Override
        public void run(){
            adapter.remove(nombre);
            ProveedorSnackBar
                    .muestraBarraDeBocados(lista, "Hecho");
        }
    }
}