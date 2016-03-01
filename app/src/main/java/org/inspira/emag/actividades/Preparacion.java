package org.inspira.emag.actividades;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.capiz.bluetooth.R;
import org.inspira.emag.fragmentos.SignUp;

/**
 * Created by jcapiz on 29/02/16.
 */
public class Preparacion extends AppCompatActivity {

    private SignUp signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        signUp = new SignUp();
        colocaFragmento();
    }

    private void colocaFragmento() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.preparacion_main_container, signUp)
                .commit();
    }
}
