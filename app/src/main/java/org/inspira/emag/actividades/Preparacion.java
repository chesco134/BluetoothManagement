package org.inspira.emag.actividades;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.capiz.bluetooth.R;
import org.inspira.emag.fragmentos.SignIn;

/**
 * Created by jcapiz on 29/02/16.
 */
public class Preparacion extends AppCompatActivity {

    private SignIn signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        if(savedInstanceState == null) {
            signIn = new SignIn();
            colocaFragmento();
        }
    }

    private void colocaFragmento() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.preparacion_main_container, signIn)
                .commit();
    }
}
