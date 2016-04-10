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
import org.inspira.emag.dialogos.ActividadDeEspera;
import org.inspira.emag.dialogos.DialogoDeConsultaSimple;
import org.inspira.emag.dialogos.ObtenerTexto;
import org.inspira.emag.dialogos.ProveedorSnackBar;
import org.inspira.emag.dialogos.RemueveElementosDeLista;
import org.inspira.emag.networking.ActualizaVehiculoPrincipal;
import org.inspira.emag.networking.AltaVehiculo;
import org.inspira.emag.networking.ObtencionDeAutos;
import org.inspira.emag.service.ObdMainService;
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
        String nombreVehiculo = ProveedorDeRecursos.obtenerRecursoString(this, "vehiculo");
                ((TextView) findViewById(R.id.perfiles_de_autos_auto_actual)).setText(nombreVehiculo);
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

    private void updateVehiculo(final String nombreDeVehiculo) {
        final String email = ProveedorDeRecursos.obtenerRecursoString(OrganizarVehiculos.this, "email");
        final int idVehiculo = new TripsData(this).obtenerIdVehiculoFromNombre(nombreDeVehiculo);
        ActualizaVehiculoPrincipal avp = new ActualizaVehiculoPrincipal(this, email, idVehiculo);
        avp.setAcciones(new ObtencionDeAutos.AccionesObtencionDeConvocatorias() {
            @Override
            public void obtencionCorrecta(JSONObject json) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProveedorSnackBar
                                .muestraBarraDeBocados(lista, "Hecho");
                    }
                });
                ProveedorDeRecursos.guardarRecursoString(OrganizarVehiculos.this, "vehiculo", nombreDeVehiculo);
                Log.d("Morder", "idVehiculo: " + idVehiculo + ", nombre: " + nombreDeVehiculo);
                finishActivity(1234);
            }

            @Override
            public void obtencionIncorrecta(final String mensaje) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProveedorSnackBar
                                .muestraBarraDeBocados(lista, mensaje);
                    }
                });
                finishActivity(1234);
            }
        });
        avp.start();
        startActivityForResult(new Intent(this, ActividadDeEspera.class), 1234);
    }

    public void actualizarEtiquetaVehiculoPrincipal(String nuevoValor){
        ((TextView) findViewById(R.id.perfiles_de_autos_auto_actual)).setText(nuevoValor);
    }

    private void launchDialogoRemoverElementos() {
        RemueveElementosDeLista rm = new RemueveElementosDeLista();
        Bundle args = new Bundle();
        final List<String> elementos = new ArrayList<>();
        final String vActual = ProveedorDeRecursos.obtenerRecursoString(this, "vehiculo");
        final Vehiculo[] vehiculos = new TripsData(this).obtenerVehiculosValidos();
        for(Vehiculo v : vehiculos)
            if(!vActual.equals(v.getNombre())) elementos.add(v.getNombre());
        args.putStringArray("elementos", elementos.toArray(new String[]{}));
        rm.setArguments(args);
        rm.setAd(new RemueveElementosDeLista.AccionDialogo() {
            @Override
            public void accionPositiva(DialogFragment fragment) {
                Integer[] indices = ((RemueveElementosDeLista) fragment).getElementosSeleccionados();
                prepareElements(indices);
                final List<Vehiculo> sublist = new ArrayList<>();
                for (Integer index : indices) {
                    sublist.add(vehiculos[index]);
                }
                final TripsData db = new TripsData(OrganizarVehiculos.this);
                db.colocarVehiculosEnNoBorrado(sublist.toArray(new Vehiculo[]{}));
                new Thread() {
                    @Override
                    public void run() {
                        for (Vehiculo vehiculo : sublist)
                            try {
                                JSONObject json = new JSONObject();
                                json.put("action", 9); // Solicitud de borrar vehículo para el usuario.
                                json.put("idVehiculo", vehiculo.getIdVehiculo());
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
                                Log.d("Momonga", "Vehiculo eliminado: " + baos.toString());
                                json = new JSONObject(baos.toString());
                                baos.close();
                                if (json.getBoolean("content")) {
                                    db.removerVehiculo(vehiculo.getIdVehiculo());
                                    runOnUiThread(new Poster(vehiculo.getNombre()));
                                }
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                    }
                }.start();
            }

            @Override
            public void accionNegativa(DialogFragment fragment) {
            }
        });
        rm.show(getSupportFragmentManager(), "Remover elementos");
    }

    public void agregarElemento(final String nuevoVehiculo){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(nuevoVehiculo);
            }
        });
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
                if(!"".equals(texto.trim())) {
                    new AltaVehiculo(OrganizarVehiculos.this, texto).start();
                }else{
                    ProveedorSnackBar
                            .muestraBarraDeBocados(lista, "Es necesario un nombre");
                }
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