package org.inspira.emag.fragmentos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.capiz.bluetooth.R;
import org.inspira.emag.actividades.Espera;
import org.inspira.emag.actividades.MainActivity;
import org.inspira.emag.actividades.OrganizarVehiculos;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.dialogos.DialogoDeConsultaSimple;
import org.inspira.emag.dialogos.ObtenerFecha;
import org.inspira.emag.dialogos.ProveedorSnackBar;
import org.inspira.emag.seguridad.Hasher;
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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by jcapiz on 29/02/16.
 */
public class SignUp extends Fragment {

    private static final int ESPERA = 231;
    private EditText email;
    private EditText nickname;
    private EditText carNickname;
    private EditText pass;
    private TextView date;
    private long fechaDeNacimiento;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.signup, parent, false);
        email = (EditText) rootView.findViewById(R.id.signup_email);
        nickname = (EditText) rootView.findViewById(R.id.signup_usuario);
        carNickname = (EditText) rootView.findViewById(R.id.signup_car_nickname);
        pass = (EditText) rootView.findViewById(R.id.signup_pass);
        pass.addTextChangedListener(new MyWatcher());
        date = (TextView) rootView.findViewById(R.id.signup_fecha_de_nacimiento);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDialogoFecha();
            }

            private void launchDialogoFecha() {
                ObtenerFecha of = new ObtenerFecha();
                of.setAgenteDeInteraccion(new DialogoDeConsultaSimple.AgenteDeInteraccionConResultado() {
                    @Override
                    public void clickSobreAccionPositiva(DialogFragment dialogo) {
                        fechaDeNacimiento = ((ObtenerFecha) dialogo).getFecha().getTime();
                        date.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date(fechaDeNacimiento)));
                    }

                    @Override
                    public void clickSobreAccionNegativa(DialogFragment dialogo) {
                    }
                });
                of.show(getActivity().getSupportFragmentManager(), "More");
            }
        });
        rootView.findViewById(R.id.signup_registrar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarInformacion();
            }
        });
        if(savedInstanceState == null){
            fechaDeNacimiento = 0;
        }else{
            fechaDeNacimiento = savedInstanceState.getLong("fecha_de_nacimiento");
            email.setText(savedInstanceState.getString("email"));
            nickname.setText(savedInstanceState.getString("nickname"));
            carNickname.setText(savedInstanceState.getString("car_nickname"));
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putLong("fecha_de_nacimiento", fechaDeNacimiento);
        outState.putString("email", email.getText().toString());
        outState.putString("nickname", nickname.getText().toString());
        outState.putString("car_nickname", carNickname.getText().toString());
    }

    private void validarInformacion() {
        Intent i = new Intent(getContext(), Espera.class);
        i.putExtra("message", "Conectando");
        //startActivityForResult(i, ESPERA);
        final String mail = email.getText().toString();
        final String name = nickname.getText().toString();
        final String car = carNickname.getText().toString();
        final User user = new User();
        user.setEmail(mail);
        user.setNickname(name);
        user.setPass(new Hasher().makeHash(pass.getText().toString()));
        user.setDateOfBirth(fechaDeNacimiento);
        new Thread(){
            @Override
            public void run(){
                if(!"".equals(mail)
                        && !"".equals(name)
                        && !"".equals(car)
                        && user.getPass().length > 5
                        && 0 != fechaDeNacimiento
                        && validarRemotamente(user)){
                    guardarInformacion(user);
                    colocarPantallaPrincipal();
                }else{

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finishActivity(ESPERA);
                    }
                });
            }
        }.start();
    }

    private void colocarPantallaPrincipal() {
        getActivity().finish();
    }

    private void guardarInformacion(User usuario) {
        TripsData db = new TripsData(getContext());
        db.addUser(usuario);
        Vehiculo v = new Vehiculo();
        v.setNombre(carNickname.getText().toString());
        v.setEmail(usuario.getEmail());
        db.addVehiculo(v);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putString("vehiculo", v.getNombre());
    }

    private boolean validarRemotamente(User user) {
        boolean veredicto = false;
        try{
            JSONObject json = new JSONObject();
            json.put("action",7); // La acción 5 es para solicitar una validación de datos.
            json.put("email", user.getEmail());
            json.put("nickname", user.getNickname());
            json.put("fecha_de_nacimiento", user.getDateOfBirth());
            json.put("pass", Arrays.toString(user.getPass()));
            HttpURLConnection con = (HttpURLConnection) new URL(MainActivity.SERVER_URL).openConnection();
            con.setDoOutput(true);
            DataOutputStream salida = new DataOutputStream(con.getOutputStream());
            salida.write(json.toString().getBytes());
            salida.flush();
            DataInputStream entrada = new DataInputStream(con.getInputStream());
            int length;
            byte[] chunk = new byte[64];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((length = entrada.read(chunk)) != -1)
                baos.write(chunk,0,length);
            Log.d("Momonga", URLDecoder.decode(baos.toString(),"utf8"));
            final JSONObject respuesta = new JSONObject(URLDecoder.decode(baos.toString(), "utf8"));
            baos.close();
            veredicto = respuesta.getBoolean("content");
            con.disconnect();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProveedorSnackBar
                                .muestraBarraDeBocados(email, respuesta.getString("mensaje"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch(JSONException | IOException e){
            e.printStackTrace();
        }
        return veredicto;
    }

    private class MyWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(pass.getText().toString().matches("^(?=.*\\d)(?=.*[a-z])(?=.*[@!\"\\.\\$#%&/()])(?=.*[A-Z]).{5}$"))
                pass.setBackgroundColor(getActivity().getResources().getColor(R.color.actionbar_text));
            else
                pass.setBackgroundColor(getActivity().getResources().getColor(R.color.error));
        }
    }
}