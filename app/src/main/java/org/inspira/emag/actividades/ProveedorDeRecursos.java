package org.inspira.emag.actividades;

import android.content.Context;

/**
 * Created by jcapiz on 22/03/16.
 */
public class ProveedorDeRecursos {

    public static String obtenerRecursoString(Context context, String recurso){
        return context.getSharedPreferences(OrganizarVehiculos.class.getName(), Context.MODE_PRIVATE)
                .getString(recurso, "NaN");
    }
}
