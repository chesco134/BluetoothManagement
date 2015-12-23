package org.inspira.emag.shared;

import android.content.Context;

import org.inspira.emag.database.TripsData;

/**
 * Created by jcapiz on 18/11/15.
 */
public class Trip implements Shareable{

    private int idTrip;
    private String fechaInicio;
    private String fechaFin;

    public Trip(int idTrip, String fechaInicio, String fechaFin){
        this.idTrip = idTrip;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public int getIdTrip(){
        return idTrip;
    }

    public String getFechaInicio(){
        return fechaInicio;
    }

    public String getFechaFin(){
        return fechaFin;
    }

    @Override
    public void commitEntry(Context ctx) {
        TripsData db = new TripsData(ctx);
        db.setCommited("Trip",idTrip);
    }
}
