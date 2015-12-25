package org.inspira.emag.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.inspira.emag.shared.Location;
import org.inspira.emag.shared.RPM;
import org.inspira.emag.shared.Speed;
import org.inspira.emag.shared.ThrottlePos;
import org.inspira.emag.shared.Trip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TripsData extends SQLiteOpenHelper{

	public TripsData(Context context){
		super(context, "EMAG", null, 1);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public void onCreate(SQLiteDatabase dataBase) {
		dataBase.execSQL("create table Trip(idTrip INTEGER PRIMARY KEY AUTOINCREMENT, fechaInicio TEXT NOT NULL, fechaFin TEXT, isCommited INTEGER DEFAULT 0)");
		dataBase.execSQL("create table RPM(idValue INTEGER PRIMARY KEY AUTOINCREMENT, RPMVal TEXT NOT NULL, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table Speed(idValue INTEGER PRIMARY KEY AUTOINCREMENT, SpeedVal TEXT NOT NULL, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table ThrottlePos(idValue INTEGER PRIMARY KEY AUTOINCREMENT, ThrottleVal TEXT NOT NULL, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table Location(idValue INTEGER PRIMARY KEY AUTOINCREMENT, Latitud TEXT NOT NULL, Longitud TEXT NOT NULL, Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
	}

	public void setCommited(String table, int idTrip){
		Log.d("XXXXXXXXXXXXX", "Commiting " + table + ", " + idTrip);
		ContentValues val = new ContentValues();
		val.put("isCommited", "1");
		String where = "idTrip=?";
		String whereArgs[] = {String.valueOf(idTrip)};
		getWritableDatabase().update(table, val, where, whereArgs);
		close();
	}

	public long insertTrip(Date fechaInicio){
		ContentValues values = new ContentValues();
		values.put("fechaInicio", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(fechaInicio));
		long id = getWritableDatabase().insert("Trip","---",values);
		close();
		return id;
	}

	public void terminaFechaTrip(int idTrip, Date fechaFin){
		ContentValues values = new ContentValues();
		values.put("fechaFin", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(fechaFin));
		values.put("isCommited","1");
		String[] whereArgs = {String.valueOf(idTrip)};
		getWritableDatabase().update("Trip",values,"idTrip = ?",whereArgs);
		Log.d("TripsData", "Trip " + idTrip + "commited.");
		close();
	}

	public int insertaRPM(String rpm, int idTrip){
		ContentValues values = new ContentValues();
		values.put("RPMVal",rpm);
		values.put("idTrip", idTrip);
		int lrid = -1;
		long id = getWritableDatabase().insert("RPM", "---", values);
		if(id > -1) {
			Cursor c = getReadableDatabase().rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
		}
		close();
		return lrid;
	}

	public int insertaVelocidad(String velocidad, int idTrip){
		ContentValues values = new ContentValues();
		values.put("SpeedVal",velocidad);
		values.put("idTrip", idTrip);
		int lrid = -1;
		long id = getWritableDatabase().insert("Speed", "---", values);
		if(id > -1) {
			Cursor c = getReadableDatabase().rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
		}
		close();
		return lrid;
	}

	public int insertaThrottlePos(String throttleVal, int idTrip){
		ContentValues values = new ContentValues();
		values.put("ThrottleVal",throttleVal);
		values.put("idTrip", idTrip);
		int lrid = -1;
		long id = getWritableDatabase().insert("ThrottlePos", "---", values);
		if(id > -1) {
			Cursor c = getReadableDatabase().rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
		}
		close();
		return lrid;
	}

	public int insertaUbicacion(String latitud,String longitud, int idTrip){
		ContentValues values = new ContentValues();
		values.put("Latitud",latitud);
		values.put("Longitud", longitud);
		values.put("idTrip", idTrip);
		int lrid = -1;
		long id = getWritableDatabase().insert("Location","---",values);
		if(id > -1) {
			Cursor c = getReadableDatabase().rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
		}
		close();
		return lrid;
	}

	public Trip[] getTrips(){
		Cursor c = getReadableDatabase().rawQuery("select * from Trip",null);
		List<Trip> trips = new ArrayList<Trip>();
		while(c.moveToNext())
			trips.add(new Trip(c.getInt(c.getColumnIndex("idTrip")), (c.getString(c.getColumnIndex("fechaInicio"))),(c.getString(c.getColumnIndex("fechaFin")))));
		c.close();
		close();
		return trips.toArray(new Trip[0]);
	}

	public Trip getUnconcludedTrip(){
		Trip lastTrip = null;
		Cursor c = getReadableDatabase().rawQuery("select * from Trip where fechaFin = '---' or fechaFin is null",null);
		if(c.getCount() > 0){
			c.moveToLast();
			lastTrip = new Trip(c.getInt(c.getColumnIndex("idTrip")),(c.getString(c.getColumnIndex("fechaInicio"))),(c.getString(c.getColumnIndex("fechaFin"))));
		}
		return lastTrip;
	}

	public Trip[] getUncommitedTrips(){
		List<Trip> trips = new ArrayList<Trip>();
		Cursor c = getReadableDatabase().rawQuery("select * from Trip where isCommited = 0",null);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		while(c.moveToNext()){
			trips.add(new Trip(c.getInt(c.getColumnIndex("idTrip")), (c.getString(c.getColumnIndex("fechaInicio"))), (c.getString(c.getColumnIndex("fechaFin")))));
		}
		c.close();
		close();
		return trips.toArray(new Trip[0]);
	}

	public RPM[] getRPMsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from RPM where idTrip = ? and isCommited = 0", new String[]{String.valueOf(idTrip)});
		List<RPM> rpms = new ArrayList<RPM>();
		while(c.moveToNext()){
			rpms.add(new RPM(c.getInt(c.getColumnIndex("idValue")),c.getString(c.getColumnIndex("RPMVal")),c.getString(c.getColumnIndex("Timestamp")),c.getInt(c.getColumnIndex("idTrip"))));
		}
		c.close();
		close();
		return rpms.toArray(new RPM[0]);
	}

	public Speed[] getSpeedsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from Speed where idTrip = ? and isCommited = 0", new String[]{String.valueOf(idTrip)});
		List<Speed> speeds = new ArrayList<Speed>();
		while(c.moveToNext()){
			speeds.add(new Speed(c.getInt(c.getColumnIndex("idValue")),c.getString(c.getColumnIndex("SpeedVal")),c.getString(c.getColumnIndex("Timestamp")),c.getInt(c.getColumnIndex("idTrip"))));
		}
		c.close();
		close();
		return speeds.toArray(new Speed[0]);
	}

	public ThrottlePos[] getThrottlePosByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from ThrottlePos where idTrip = ? and isCommited = 0", new String[]{String.valueOf(idTrip)});
		List<ThrottlePos> throttlePoses = new ArrayList<ThrottlePos>();
		while(c.moveToNext()){
			throttlePoses.add(new ThrottlePos(c.getInt(c.getColumnIndex("idValue")), c.getString(c.getColumnIndex("ThrottleVal")), c.getString(c.getColumnIndex("Timestamp")), c.getInt(c.getColumnIndex("idTrip"))));
		}
		c.close();
		close();
		return throttlePoses.toArray(new ThrottlePos[0]);
	}

	public Location[] getLocationsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from Location where idTrip = ? and isCommited = 0", new String[]{String.valueOf(idTrip)});
		List<Location> locations = new ArrayList<Location>();
		while(c.moveToNext()){
			locations.add(new Location(c.getInt(c.getColumnIndex("idValue")),c.getString(c.getColumnIndex("Latitud")),c.getString(c.getColumnIndex("Longitud")),c.getString(c.getColumnIndex("Timestamp")),c.getInt(c.getColumnIndex("idTrip"))));
		}
		c.close();
		close();
		return locations.toArray(new Location[]{});
	}

	public void clearTables(){
		SQLiteDatabase db = getWritableDatabase();
		db.delete("RPM",null,null);
		db.delete("Location",null,null);
		db.delete("Speed",null,null);
		db.delete("ThrottlePos",null,null);
		db.delete("Trip",null,null);
	}
}