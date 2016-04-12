package org.inspira.emag.shared;

import android.content.Context;

import org.inspira.emag.database.TripsData;

/**
 * Created by jcapiz on 18/11/15.
 */
public class Speed implements Shareable{

    private int idValue;
    private String speed;
    private String timestamp;
    private int idTrip;
    private boolean isCommited;

    public Speed(int idValue, String speed, String timestamp, int idTrip) {
        this.idValue = idValue;
        this.speed = speed;
        this.timestamp = timestamp;
        this.idTrip = idTrip;
    }

    public int getIdValue() {
        return idValue;
    }

    public String getSpeed() {
        return speed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getIdTrip() {
        return idTrip;
    }

    public boolean isCommited() {
        return isCommited;
    }

    public void setIsCommited(boolean isCommited) {
        this.isCommited = isCommited;
    }

    @Override
    public void commitEntry(Context ctx) {
        TripsData db = new TripsData(ctx);
        db.setCommited("Speed",idTrip);
    }
}
