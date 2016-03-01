package org.inspira.emag.shared;

import android.content.Context;

/**
 * Created by jcapiz on 29/02/16.
 */
public class Vehiculo implements Shareable {

    private String nombre;
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void commitEntry(Context ctx) {

    }
}
