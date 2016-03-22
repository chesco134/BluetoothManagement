package org.inspira.emag.fragmentos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import org.inspira.emag.networking.AltaVehiculo;
import org.inspira.emag.seguridad.Hasher;
import org.inspira.emag.shared.User;
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
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private Button confirm;
    private long fechaDeNacimiento;

    private MyWatcher myWatcher;
    private MyMailWatcher mailWatcher;
    private boolean cStatus;
    private Acciones acciones;

    public void setAcciones(Acciones acciones) {
        this.acciones = acciones;
    }

    public interface Acciones{
        void onResume(String mensaje);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.signup, parent, false);
        email = (EditText) rootView.findViewById(R.id.signup_email);
        mailWatcher = new MyMailWatcher();
        email.addTextChangedListener(mailWatcher);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email.setBackgroundColor(getActivity().getResources().getColor(R.color.actionbar_text));
            }
        });
        nickname = (EditText) rootView.findViewById(R.id.signup_usuario);
        carNickname = (EditText) rootView.findViewById(R.id.signup_car_nickname);
        pass = (EditText) rootView.findViewById(R.id.signup_pass);
        pass.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Regular.ttf"));
        myWatcher = new MyWatcher();
        pass.addTextChangedListener(myWatcher);
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
                        Calendar c = Calendar.getInstance();
                        if (c.getTimeInMillis() - fechaDeNacimiento >= 5045760e5)
                            date.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date(fechaDeNacimiento)));
                        else
                            ProveedorSnackBar
                                    .muestraBarraDeBocados(email, "La edad mínima son 16 años");
                    }

                    @Override
                    public void clickSobreAccionNegativa(DialogFragment dialogo) {
                    }
                });
                of.show(getActivity().getSupportFragmentManager(), "More");
            }
        });
        confirm = (Button) rootView.findViewById(R.id.signup_registrar);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cStatus = false;
                v.setEnabled(cStatus);
                validarInformacion();
            }
        });
        if(savedInstanceState == null){
            fechaDeNacimiento = 0;
            cStatus = true;
        }else{
            fechaDeNacimiento = savedInstanceState.getLong("fecha_de_nacimiento");
            email.setText(savedInstanceState.getString("email"));
            nickname.setText(savedInstanceState.getString("nickname"));
            carNickname.setText(savedInstanceState.getString("car_nickname"));
            cStatus = savedInstanceState.getBoolean("confirm_status");
        }
        confirm.setEnabled(cStatus);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putLong("fecha_de_nacimiento", fechaDeNacimiento);
        outState.putString("email", email.getText().toString());
        outState.putString("nickname", nickname.getText().toString());
        outState.putString("car_nickname", carNickname.getText().toString());
        outState.putBoolean("confirm_status", cStatus);
    }

    @Override
    public void onResume(){
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Registro");
    }

    private void validarInformacion() {
        boolean veredicto = true;
        if( !myWatcher.isEnabled() ){
            ProveedorSnackBar
                    .muestraBarraDeBocados(email, "Los campos en rojo son necesarios");
            veredicto = false;
        }
        if( !mailWatcher.isEnabled() ){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    email.setBackgroundColor(getActivity().getResources().getColor(R.color.error));
                }
            });
            ProveedorSnackBar
                    .muestraBarraDeBocados(email, "Los campos en rojo son necesarios");
            veredicto = false;
        }
        if(veredicto) {
            Intent i = new Intent(getContext(), Espera.class);
            i.putExtra("message", "Conectando");
            //startActivityForResult(i, ESPERA);
            final String mail = email.getText().toString();
            final String name = nickname.getText().toString();
            final String car = carNickname.getText().toString();
            final User user = new User();
            user.setEmail(mail);
            user.setNickname(name);
            user.setPass(new Hasher().makeHashString(pass.getText().toString()));
            user.setDateOfBirth(fechaDeNacimiento);
            new Thread() {
                @Override
                public void run() {
                    if (!"".equals(mail)
                            && !"".equals(name)
                            && !"".equals(car)
                            && !"".equals(pass.getText().toString())
                            && 0 != fechaDeNacimiento
                            && validarRemotamente(user)) {
                        guardarInformacion(user);
                        colocarPantallaPrincipal();
                    } else {

                    }
                    cStatus = true;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            confirm.setEnabled(cStatus);
                        }
                    });
                }
            }.start();
        }
    }

    private void colocarPantallaPrincipal() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private void guardarInformacion(User usuario) {
        TripsData db = new TripsData(getContext());
        db.addUser(usuario);
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE).edit();
        editor.putString("vehiculo", carNickname.getText().toString());
        editor.putString("email", usuario.getEmail());
        editor.apply();
        AltaVehiculo alta = new AltaVehiculo(getActivity(), carNickname.getText().toString());
        alta.setEsPrincipal(true);
        alta.start();
    }

    private boolean validarRemotamente(User user) {
        boolean veredicto = false;
        try{
            JSONObject json = new JSONObject();
            json.put("action",7); // La acción 7 es para solicitar una validación de datos.
            json.put("email", user.getEmail());
            json.put("nickname", user.getNickname());
            json.put("fecha_de_nacimiento", user.getDateOfBirth());
            json.put("pass", user.getPass());
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
            Log.d("Momonga", URLDecoder.decode(baos.toString(), "utf8"));
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

        private boolean enabled = false;
        private Pattern pattern;
        private Matcher matcher;
        private static final String PASS_PATTERN =
                "(?=.*[A-Z])(?=.*[!@#$&*])(?=.*[0-9])(?=.*[a-z]).{5,}";

        public MyWatcher() {
            pattern = Pattern.compile(PASS_PATTERN);
        }

        public boolean validate(final String hex) {
            matcher = pattern.matcher(hex);
            return matcher.matches();
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if((pass.getText().toString().length() > 5)) {
                pass.setBackgroundColor(getActivity().getResources().getColor(R.color.actionbar_text));
                enabled = true;
            }else {
                pass.setBackgroundColor(getActivity().getResources().getColor(R.color.error));
                enabled = false;
            }
        }
    }

    private class MyMailWatcher implements TextWatcher{

        private boolean enabled = false;
        private Pattern pattern;
        private Matcher matcher;
        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        public MyMailWatcher() {
            pattern = Pattern.compile(EMAIL_PATTERN);
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean validate(final String hex) {
            matcher = pattern.matcher(hex);
            return matcher.matches();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (validate(email.getText().toString())) {
                email.setBackgroundColor(getActivity().getResources().getColor(R.color.actionbar_text));
                enabled = true;
            }else {
                enabled = false;
            }
        }
    }
}