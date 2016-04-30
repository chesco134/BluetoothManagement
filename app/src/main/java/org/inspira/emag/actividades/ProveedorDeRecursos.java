package org.inspira.emag.actividades;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

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

    public static String obtenerFecha(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth)
                + "/" +
                (month < 10 ? "0" + month : month)
                + "/" +
                year
                + " " +
                (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                + ":" +
                (minute < 10 ? "0" + minute : minute)
                + ":" +
                (second < 10 ? "0" + second : second);
    }
}
