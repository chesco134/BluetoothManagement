package org.inspira.emag.shared;

import android.content.Context;

import org.inspira.emag.database.TripsData;

/**
 * Created by jcapiz on 18/11/15.
 */
public class RPM implements Shareable{

    private int idValue;
    private String rpmValue;
    private String timeStamp;
    private int idTrip;

    public RPM(int idValue, String rpmValue, String timeStamp, int idTrip) {
        this.idValue = idValue;
        this.rpmValue = rpmValue;
        this.timeStamp = timeStamp;
        this.idTrip = idTrip;
    }

    public int getIdValue() {
        return idValue;
    }

    public String getRpmValue() {
        return rpmValue;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public int getIdTrip() {
        return idTrip;
    }

    @Override
    public void commitEntry(Context ctx) {
        TripsData db = new TripsData(ctx);
        db.setCommited("RPM",idTrip);
    }
}
