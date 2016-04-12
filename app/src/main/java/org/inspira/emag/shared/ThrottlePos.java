package org.inspira.emag.shared;

import android.content.Context;

import org.inspira.emag.database.TripsData;

/**
 * Created by jcapiz on 24/12/15.
 */
public class ThrottlePos implements Shareable {


    private int idValue;
    private String throttlePos;
    private String timestamp;
    private int idTrip;
    private boolean isCommited;

    public ThrottlePos(int idValue, String throttlePos, String timestamp, int idTrip) {
        this.idValue = idValue;
        this.throttlePos = throttlePos;
        this.timestamp = timestamp;
        this.idTrip = idTrip;
    }

    public int getIdValue() {
        return idValue;
    }

    public String getThrottlePos() {
        return throttlePos;
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
        db.setCommited("ThrottlePos",idTrip);
    }
}