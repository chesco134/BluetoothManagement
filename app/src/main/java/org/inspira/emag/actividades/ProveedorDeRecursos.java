package org.inspira.emag.actividades;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jcapiz on 22/03/16.
 */
public class ProveedorDeRecursos {

    private static final String NOMBRE_SETTINGS = OrganizarVehiculos.class.getName();

    public static String obtenerRecursoString(Context context, String recurso){
        return context.getSharedPreferences(NOMBRE_SETTINGS, Context.MODE_PRIVATE)
                .getString(recurso, "NaN");
    }

    public static void guardarRecursoString(Context context, String recurso, String valor){
        SharedPreferences.Editor editor = context.getSharedPreferences(NOMBRE_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putString(recurso, valor);
        editor.apply();
    }
}
