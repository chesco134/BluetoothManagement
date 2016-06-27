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
import org.inspira.emag.shared.User;
import org.inspira.emag.shared.Vehiculo;

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
		dataBase.execSQL("create table User(" +
				"email TEXT NOT NULL PRIMARY KEY," +
				"nickname TEXT NOT NULL," +
				"dateOfBirth long not null" +
				")");
		dataBase.execSQL("create table Vehiculo(" +
				"idVehiculo INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT NOT NULL," +
                "estadoServidorRemoto INTEGER DEFAULT 0," + // 0 es no agregado, 1 es agregado, 2 no borrado
                "email TEXT NOT NULL," +
                "FOREIGN KEY(email) REFERENCES User(email)" +
                ")");
		dataBase.execSQL("create table Trip(" +
                "idTrip INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fechaInicio TEXT NOT NULL, fechaFin TEXT, " +
                "isCommited INTEGER DEFAULT 0," +
                "idVehiculo INTEGER NOT NULL," +
                "FOREIGN KEY(idVehiculo) REFERENCES Vehiculo(idVehiculo)" +
                ")");
		dataBase.execSQL("create table RPM(idValue INTEGER PRIMARY KEY AUTOINCREMENT, RPMVal TEXT NOT NULL, Timestamp TEXT, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table Speed(idValue INTEGER PRIMARY KEY AUTOINCREMENT, SpeedVal TEXT NOT NULL, Timestamp TEXT, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table ThrottlePos(idValue INTEGER PRIMARY KEY AUTOINCREMENT, ThrottleVal TEXT NOT NULL, Timestamp TEXT, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
		dataBase.execSQL("create table Location(idValue INTEGER PRIMARY KEY AUTOINCREMENT, Latitud TEXT NOT NULL, Longitud TEXT NOT NULL, Timestamp TEXT, isCommited INTEGER DEFAULT 0, idTrip INTEGER NOT NULL, FOREIGN KEY(idTrip) REFERENCES Trip(idTrip))");
	}

    public boolean addVehiculo(Vehiculo vehiculo){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from Vehiculo where nombre like ?",
                new String[]{vehiculo.getNombre()});
        boolean exists;
        if(!(exists = c.moveToFirst())) {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("nombre", vehiculo.getNombre());
            values.put("email", vehiculo.getEmail());
			values.put("idVehiculo", vehiculo.getIdVehiculo());
            db.insert("Vehiculo", "---", values);
        }
        c.close();
        db.close();
        return !exists;
    }

    public Vehiculo[] obtenerVehiculosValidos(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from Vehiculo where estadoServidorRemoto != 2", null);
        Vehiculo vehiculo;
        List<Vehiculo> vehiculos = new ArrayList<>();
        while(c.moveToNext()){
            vehiculo = new Vehiculo();
            vehiculo.setEmail(c.getString(c.getColumnIndex("email")));
            vehiculo.setNombre(c.getString(c.getColumnIndex("nombre")));
			vehiculo.setIdVehiculo(c.getInt(c.getColumnIndex("idVehiculo")));
            vehiculos.add(vehiculo);
        }
        c.close();
        db.close();
        return vehiculos.toArray(new Vehiculo[]{});
    }

    public int obtenerIdVehiculoFromNombre(String nombre){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select idVehiculo from Vehiculo where nombre like ?",
                new String[]{nombre});
        int vid = -1;
        if(c.moveToFirst()){
            vid = c.getInt(0);
        }
        c.close();
        db.close();
        return vid;
    }

    public void colocarVehiculosEnNoBorrado(Vehiculo[] vehiculos){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estadoServidorRemoto", 2);
        String[] whereVals = new String[1];
        for(Vehiculo vehiculo : vehiculos){
            whereVals[0] = String.valueOf(vehiculo.getIdVehiculo());
            db.update("Vehiculo", values, "idVehiculo = CAST(? as INTEGER)", whereVals);
        }
        db.close();
    }

	public void removerVehiculo(int idVehiculo){
		SQLiteDatabase db = getWritableDatabase();
		db.delete("Vehiculo", "idVehiculo = CAST(? as INTEGER)", new String[]{String.valueOf(idVehiculo)});
		db.close();
	}

	public void addUser(User user){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("email", user.getEmail());
        values.put("nickname", user.getNickname());
        values.put("dateOfBirth", user.getDateOfBirth());
        db.insert("User", "---", values);
        db.close();
	}

    public User getUserData(){
        User user;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from User limit 1", null);
        if(c.moveToFirst()){
            user = new User();
            user.setEmail(c.getString(c.getColumnIndex("email")));
            user.setDateOfBirth(c.getLong(c.getColumnIndex("dateOfBirth")));
            user.setNickname(c.getString(c.getColumnIndex("nickname")));
        }else{
            user = null;
        }
		c.close();
		db.close();
        return user;
    }

    public boolean userCheck(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from User", null);
        boolean exists = c.moveToNext();
        c.close();
		db.close();
        return exists;
    }

	public void setCommited(String table, int idTrip){
		Log.d("XXXXXXXXXXXXX", "Commiting " + table + ", " + idTrip);
		ContentValues val = new ContentValues();
		val.put("isCommited", 1);
		String where = "idTrip= CAST(? as INTEGER)";
		String whereArgs[] = {String.valueOf(idTrip)};
		SQLiteDatabase db = getWritableDatabase();
		db.update(table, val, where, whereArgs);
		db.close();
	}

	public int insertTrip(int idVehiculo, String fechaInicio){
        SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("fechaInicio", fechaInicio);
        values.put("idVehiculo", idVehiculo);
		db.insert("Trip", "---", values);
		db.close();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("select last_insert_rowid()", null);
        int id = -1;
        if(c.moveToFirst()){
            id = c.getInt(0);
        }
        c.close();
		db.close();
		return id;
	}

	public void terminaFechaTrip(int idTrip, Date fechaFin){
		ContentValues values = new ContentValues();
		values.put("fechaFin", new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(fechaFin));
		values.put("isCommited",1);
		String[] whereArgs = {String.valueOf(idTrip)};
		SQLiteDatabase db = getWritableDatabase();
		db.update("Trip", values, "idTrip = CAST(? as INTEGER)", whereArgs);
		Log.d("TripsData", "Trip #" + idTrip + "commited.");
		db.close();
	}

	public int insertaRPM(String rpm, String time, int idTrip){
		ContentValues values = new ContentValues();
		values.put("RPMVal", rpm);
		values.put("Timestamp", time);
		values.put("idTrip", idTrip);
		int lrid = -1;
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert("RPM", "---", values);
		if(id > -1) {
			db.close();
			db = getReadableDatabase();
			Cursor c = db.rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
			db.close();
		}else
			db.close();
		return lrid;
	}

	public int insertaVelocidad(String velocidad, String time, int idTrip){
		ContentValues values = new ContentValues();
		values.put("SpeedVal", velocidad);
		values.put("Timestamp", time);
		values.put("idTrip", idTrip);
		int lrid = -1;
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert("Speed", "---", values);
		if(id > -1) {
			db.close();
			db = getReadableDatabase();
			Cursor c = db.rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
			db.close();
		}else
			db.close();
		return lrid;
	}

	public int insertaThrottlePos(String throttleVal, String time, int idTrip){
		ContentValues values = new ContentValues();
		values.put("ThrottleVal",throttleVal);
		values.put("Timestamp", time);
		values.put("idTrip", idTrip);
		int lrid = -1;
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert("ThrottlePos", "---", values);
		if(id > -1) {
			db.close();
			db = getReadableDatabase();
			Cursor c = db.rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
			db.close();
		}else
			db.close();
		return lrid;
	}

	public int insertaUbicacion(String latitud,String longitud, String time, int idTrip){
		ContentValues values = new ContentValues();
		values.put("Latitud",latitud);
		values.put("Longitud", longitud);
		values.put("Timestamp", time);
		values.put("idTrip", idTrip);
		int lrid = -1;
		SQLiteDatabase db = getWritableDatabase();
		long id = db.insert("Location","---",values);
		Log.d("InsUbicacion", "Insertando ubicación: " + latitud + ", longitud: " + longitud + ", time: " + time + ", id: " + (id));
		if(id > -1) {
			db.close();
			db = getReadableDatabase();
			Cursor c = db.rawQuery("select last_insert_rowid()", null);
			if (c.moveToFirst())
				lrid = c.getInt(0);
			c.close();
			db.close();
		}else
			db.close();
		Log.d("InsUbicacion", "El veredicto fue lrid = " + lrid);
		return lrid;
	}

	public Trip[] getTrips(){
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select * from Trip",null);
		List<Trip> trips = new ArrayList<Trip>();
		while(c.moveToNext())
			trips.add(new Trip(c.getInt(c.getColumnIndex("idTrip")), (c.getString(c.getColumnIndex("fechaInicio"))),(c.getString(c.getColumnIndex("fechaFin")))));
		c.close();
		db.close();
		return trips.toArray(new Trip[0]);
	}

	public Trip getUnconcludedTrip(){
		Trip lastTrip = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("select * from Trip where fechaFin = '---' or fechaFin is null",null);
		if(c.moveToLast()){
			lastTrip = new Trip(c.getInt(c.getColumnIndex("idTrip")),(c.getString(c.getColumnIndex("fechaInicio"))),(c.getString(c.getColumnIndex("fechaFin"))));
		}
		c.close();
		db.close();
		return lastTrip;
	}

	public Trip[] getUncommitedTrips(){
		List<Trip> trips = new ArrayList<Trip>();
		Cursor c = getReadableDatabase().rawQuery("select * from Trip where isCommited = 0",null);
		while(c.moveToNext()){
			trips.add(new Trip(c.getInt(c.getColumnIndex("idTrip")),
					(c.getString(c.getColumnIndex("fechaInicio"))),
					(c.getString(c.getColumnIndex("fechaFin")))));
		}
		c.close();
		close();
		return trips.toArray(new Trip[0]);
	}

	public RPM[] getRPMsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from RPM where idTrip = ?", new String[]{String.valueOf(idTrip)});
		List<RPM> rpms = new ArrayList<>();
		RPM rpm;
		while(c.moveToNext()){
			rpm = new RPM(c.getInt(c.getColumnIndex("idValue")),
                    c.getString(c.getColumnIndex("RPMVal")),
                    c.getString(c.getColumnIndex("Timestamp")),
                    c.getInt(c.getColumnIndex("idTrip")));
			rpm.setIsCommited(c.getInt(c.getColumnIndex("isCommited")) != 0);
			rpms.add(rpm);
		}
		c.close();
		close();
		return rpms.toArray(new RPM[0]);
	}

	public Speed[] getSpeedsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from Speed where idTrip = ?", new String[]{String.valueOf(idTrip)});
		List<Speed> speeds = new ArrayList<Speed>();
		Speed speed;
		while(c.moveToNext()){
			speed = new Speed(c.getInt(c.getColumnIndex("idValue")),c.getString(c.getColumnIndex("SpeedVal")),c.getString(c.getColumnIndex("Timestamp")),c.getInt(c.getColumnIndex("idTrip")));
			speed.setIsCommited(c.getInt(c.getColumnIndex("isCommited")) != 0);
			speeds.add(speed);
		}
		c.close();
		close();
		return speeds.toArray(new Speed[0]);
	}

	public ThrottlePos[] getThrottlePosByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from ThrottlePos where idTrip = ?", new String[]{String.valueOf(idTrip)});
		List<ThrottlePos> throttlePoses = new ArrayList<ThrottlePos>();
		ThrottlePos throttlePos;
		while(c.moveToNext()){
			throttlePos = new ThrottlePos(c.getInt(c.getColumnIndex("idValue")), c.getString(c.getColumnIndex("ThrottleVal")), c.getString(c.getColumnIndex("Timestamp")), c.getInt(c.getColumnIndex("idTrip")));
			throttlePos.setIsCommited(c.getInt(c.getColumnIndex("isCommited")) != 0);
			throttlePoses.add(throttlePos);
		}
		c.close();
		close();
		return throttlePoses.toArray(new ThrottlePos[0]);
	}

	public Location[] getLocationsByTrip(int idTrip){
		Cursor c = getReadableDatabase().rawQuery("select * from Location where idTrip = ?", new String[]{String.valueOf(idTrip)});
		List<Location> locations = new ArrayList<Location>();
		Location location;
		while(c.moveToNext()){
			location = new Location(c.getInt(c.getColumnIndex("idValue")),c.getString(c.getColumnIndex("Latitud")),c.getString(c.getColumnIndex("Longitud")),c.getString(c.getColumnIndex("Timestamp")),c.getInt(c.getColumnIndex("idTrip")));
			location.setIsCommited(c.getInt(c.getColumnIndex("isCommited")) != 0);
			locations.add(location);

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
		db.delete("ThrottlePos", null, null);
		db.delete("Trip", null, null);
		db.close();
	}

	public void hacerVehiculoValido(Vehiculo vehiculo) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("estadoServidorRemoto", 1);
		db.update("Vehiculo", values, "idVehiculo = CAST(? as INTEGER)", new String[]{String.valueOf(vehiculo.getIdVehiculo())});
		db.close();
	}
}
