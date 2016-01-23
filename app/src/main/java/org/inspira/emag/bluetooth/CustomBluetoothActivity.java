package org.inspira.emag.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.capiz.bluetooth.R;
import org.inspira.emag.console.ConsoleActivity;
import org.inspira.emag.database.TripsData;
import org.inspira.emag.networking.CommitTrip;
import org.inspira.emag.networking.Uploader;
import org.inspira.emag.service.ObdMainService;
import org.inspira.emag.shared.Location;
import org.inspira.emag.shared.Speed;
import org.inspira.emag.shared.ThrottlePos;
import org.inspira.emag.shared.Trip;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jcapiz on 15/09/15.
 */
public class CustomBluetoothActivity extends AppCompatActivity {

    public static final String DIR_VIAJE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMAG/Viajes";
    public static final String DIR_RPM = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMAG/RPMs";
    public static final String DIR_VELOCIDAD = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMAG/Velocidades";
    public static final String DIR_THROTTLE_POS = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMAG/Posiciones_Acel";
    public static final String DIR_UBICACION = Environment.getExternalStorageDirectory().getAbsolutePath() + "/EMAG/Ubicaciones";
    public static final String VIAJE = "EMAG_Viajes.csv";
    public static final String RPM = "EMAG_rpms.csv";
    public static final String THROTTLE_POS =  "EMAG_PosicionesAcel.csv";
    public static final String VELOCIDAD =  "EMAG_Velocidades.csv";
    public static final String UBICACION = "EMAG_Ubicaciones.csv";
	private static final int START_CLIENT_ACTION = 2;
	private Button clientMode;
	private TextView buttonLabel;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView speedText;
    private TextView rpmText;
    private TextView pdaText;
	private BluetoothManager manager;
	private Intent mService;
	private int backButtonCount = 1;
    private boolean serviceOn = false;
	private boolean serverActionInProgress = false;

	private void launchAction(int typeAction) {
		backButtonCount = 1;
		serverActionInProgress = true;
		switch (typeAction) {
		case START_CLIENT_ACTION:
			Intent i = new Intent(this, DevicePickerActivity.class);
			startActivityForResult(i, START_CLIENT_ACTION);
            serviceOn = true;
            clientMode.setBackgroundResource(R.drawable.on_button);
			break;
		default:
		}
	}

    private void launchConsole(){
        Intent i = new Intent(this, ConsoleActivity.class);
        startActivity(i);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth_activity);
		clientMode = (Button) findViewById(R.id.client_mode);
		buttonLabel = (TextView) findViewById(R.id.welcome);
        mLatitudeText = (TextView) findViewById(R.id.mLatitudeText);
        mLongitudeText = (TextView) findViewById(R.id.mLongitudeText);
        speedText = (TextView) findViewById(R.id.speed_value);
        rpmText = (TextView) findViewById(R.id.rpm_value);
        pdaText = (TextView) findViewById(R.id.pda_value);
		manager = new BluetoothManager(this);
		mService = new Intent(this, ObdMainService.class);
        ((TextView)findViewById(R.id.welcome)).setTypeface(Typeface.createFromAsset(getAssets(),
                "RobotoCondensed/RobotoCondensed-Regular.ttf"));
		if (manager.getBluetoothAdapter() == null) {
			Toast.makeText(this, "Bluetooth no disponible X.X",
					Toast.LENGTH_SHORT).show();
		} else {
			clientMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!manager.isBluetoothEnabled())
                        manager.enableBluetooth();
                    else if (!serviceOn)
                        launchAction(START_CLIENT_ACTION);
                    else {
                        doUnbindService();
                        stopService(mService);
                        clientMode.setBackgroundResource(R.drawable.off);
                        makeSnackbar("Lectura de datos detenida");
                        serviceOn = !serviceOn;
                    }
                }
            });
		}
	}

    public void makeSnackbar(String message){
        Snackbar.make(clientMode, message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionSyncData = menu.findItem(R.id.action_sync_data);
        TripsData db = new TripsData(this);
        Log.d("Legitimo", "We got to see " + db.getUncommitedTrips().length);
        if(db.getUncommitedTrips().length == 0) {
            actionSyncData.setVisible(false);
            actionSyncData.setEnabled(false);
        }else {
            actionSyncData.setVisible(true);
            actionSyncData.setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_go_console) {
            launchConsole();
        } else if (id == R.id.action_sync_data){
            sendUncommitedData();
        } else if (id == R.id.action_export_data){
            grabReport();
        }
        return true;
    }

    private void commitTrip(int idTrip){
        new CommitTrip(this,idTrip).start();
    }

    public void sendUncommitedData(){
        TripsData db = new TripsData(this);
        Trip[] uncommitedTrips = db.getUncommitedTrips();
        new Uploader(uncommitedTrips).start();
        for(Trip trip : uncommitedTrips) {
            new Uploader(db.getLocationsByTrip(trip.getIdTrip())).start();
            new Uploader(db.getRPMsByTrip(trip.getIdTrip())).start();
            new Uploader(db.getSpeedsByTrip(trip.getIdTrip())).start();
            if (db.getLocationsByTrip(trip.getIdTrip()).length == 0
                    && db.getRPMsByTrip(trip.getIdTrip()).length == 0
                    && db.getSpeedsByTrip(trip.getIdTrip()).length == 0) {
                commitTrip(trip.getIdTrip());
            } else
                Log.d("EMAG sendData", "Tip " + trip.getIdTrip() + "unconcluded.");
        }
    }

    public void grabReport(){
        new Thread() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            makeSnackbar("Estamos exportando los datos...");
                        }
                    });
                    Date cDate = new Date();
                    TripsData db = new TripsData(CustomBluetoothActivity.this);
                    Trip[] trips = db.getTrips();
                    org.inspira.emag.shared.RPM[] rpmsViaje;
                    ThrottlePos[] throttlePosViaje;
                    Speed[] speedsViaje;
                    Location[] locationsViaje;
                    File f = new File(Environment.getExternalStorageDirectory(), "EMAG");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    f = new File(Environment.getExternalStorageDirectory() + "/EMAG", "Viajes");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    f = new File(Environment.getExternalStorageDirectory() + "/EMAG", "RPMs");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    f = new File(Environment.getExternalStorageDirectory() + "/EMAG", "Ubicaciones");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    f = new File(Environment.getExternalStorageDirectory() + "/EMAG", "Posiciones_Acel");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    f = new File(Environment.getExternalStorageDirectory() + "/EMAG", "Velocidades");
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    PrintWriter tripsWriter = new PrintWriter(new FileWriter(new File(DIR_VIAJE + "/" + cDate + "_" + VIAJE), true));
                    PrintWriter rpmWriter = new PrintWriter(new FileWriter(new File(DIR_RPM + "/" + cDate + "_" + RPM), true));
                    PrintWriter speedsWriter = new PrintWriter(new FileWriter(new File(DIR_VELOCIDAD + "/" + cDate + "_" + VELOCIDAD), true));
                    PrintWriter throttlePosWriter = new PrintWriter(new FileWriter(new File(DIR_THROTTLE_POS + "/" + cDate + "_" + THROTTLE_POS), true));
                    PrintWriter locationWriter = new PrintWriter(new FileWriter(new File(DIR_UBICACION + "/" + cDate + "_" + UBICACION), true));
                    for (Trip trip : trips) {
                        tripsWriter.println(trip.getIdTrip() + "," + trip.getFechaInicio() + "," + trip.getFechaFin());
                        rpmsViaje = db.getRPMsByTrip(trip.getIdTrip());
                        for (org.inspira.emag.shared.RPM rpm : rpmsViaje)
                            rpmWriter.println(rpm.getIdValue() + "," + rpm.getRpmValue() + "," + rpm.getTimeStamp() + "," + rpm.getIdTrip());
                        throttlePosViaje = db.getThrottlePosByTrip(trip.getIdTrip());
                        for (ThrottlePos throttlePos : throttlePosViaje)
                            throttlePosWriter.println(throttlePos.getIdValue() + "," + throttlePos.getThrottlePos() + "," + throttlePos.getTimestamp() + "," + throttlePos.getIdTrip());
                        speedsViaje = db.getSpeedsByTrip(trip.getIdTrip());
                        for (Speed speed : speedsViaje)
                            speedsWriter.println(speed.getIdValue() + "," + speed.getSpeed() + "," + speed.getTimestamp() + "," + speed.getIdTrip());
                        locationsViaje = db.getLocationsByTrip(trip.getIdTrip());
                        for (Location location : locationsViaje)
                            locationWriter.println(location.getIdValue() + "," + location.getLatitud() + "," + location.getLongitud() + "," + location.getTimestamp() + "," + location.getIdTrip());
                    }
                    tripsWriter.close();
                    rpmWriter.close();
                    throttlePosWriter.close();
                    speedsWriter.close();
                    locationWriter.close();
                    db.clearTables();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            makeSnackbar("Archivos creados");
                        }
                    });
                } catch (IOException ex) {
                    Log.d("From FileExportingSec", ex.getMessage());
                    ex.printStackTrace();
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            makeSnackbar("No hay datos para exportar");
                        }
                    });
                }
            }
        }.start();
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("backButtonCount", backButtonCount);
		outState.putBoolean("serverActionInProgress", serverActionInProgress);
        outState.putBoolean("serviceOn", serviceOn);
        outState.putString("button_label", buttonLabel.getText().toString());
        outState.putString("latitud", mLatitudeText.getText().toString());
        outState.putString("longitud", mLongitudeText.getText().toString());
        outState.putString("speed", speedText.getText().toString());
        outState.putString("rpm", rpmText.getText().toString());
        outState.putString("pda", pdaText.getText().toString());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		backButtonCount = savedInstanceState.getInt("backButtonCount");
        buttonLabel.setText(savedInstanceState.getString("button_label"));
        mLatitudeText.setText(savedInstanceState.getString("latitud"));
        mLongitudeText.setText(savedInstanceState.getString("longitud"));
        speedText.setText(savedInstanceState.getString("speed"));
        rpmText.setText(savedInstanceState.getString("rpm"));
        pdaText.setText(savedInstanceState.getString("pda"));
		serverActionInProgress = savedInstanceState
				.getBoolean("serverActionInProgress");
        serviceOn = savedInstanceState.getBoolean("serviceOn");
        if(serviceOn){
            doBindService();
            clientMode.setBackgroundResource(R.drawable.on_button);
        }else {
            clientMode.setBackgroundResource(R.drawable.off);
        }
	}

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(serviceOn){
            doUnbindService();
            clientMode.setBackgroundResource(R.drawable.off);
        }
    }

	@Override
	public void onBackPressed() {
		if (backButtonCount < 2) {
			serverActionInProgress = false;
			backButtonCount++;
            if(serviceOn) {
                doUnbindService();
                stopService(mService);
                clientMode.setBackgroundResource(R.drawable.off);
                serviceOn = false;
                buttonLabel.setText(getResources().getString(R.string.bienvinida));
                makeSnackbar("Lectura de datos detenida");
            }else{
                makeSnackbar("Presione una vez más para salir");
            }
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
                case START_CLIENT_ACTION:
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putString("device_addr", data.getStringExtra("device_addr"));
                    clientMode.setBackgroundResource(R.drawable.on_button);
                    buttonLabel.setText(getResources().getString(R.string.detener_lectura_datos));
                    makeSnackbar("Lectura de datos iniciada");
                    manager.cancelDiscovery();
                    startService(mService);
                    doBindService();
                    break;
                case BluetoothManager.REQUEST_ENABLE_BT:
                    launchAction(START_CLIENT_ACTION);
                    break;
            }
		} else {
			clientMode.setBackgroundResource(R.drawable.off);
            serviceOn = false;
		}
	}

    public void updateLocationData(String latitud, String longitud){
        TripsData db = new TripsData(this);
        Trip trip = db.getUnconcludedTrip();
        if( trip != null ) {
            int locId = db.insertaUbicacion(latitud, longitud, trip.getIdTrip());
            Location cLoc = new Location(locId, latitud, longitud,
                    new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()), trip.getIdTrip());
            new Uploader(cLoc).start();
        }
    }

    public void turnThingsOff(){
        clientMode.setBackgroundResource(R.drawable.off);
        serviceOn = false;
        buttonLabel.setText(getResources().getString(R.string.bienvinida));
    }

    public void setLatitudeText(String latitudeText){
        mLatitudeText.setText(latitudeText);
    }

    public void setLongitudeText(String longitudeText){
        mLongitudeText.setText(longitudeText);
    }

    public void setSpeedText(String speed){
        speedText.setText(speed);
    }

    public void setRpmText(String rpm){
        rpmText.setText(rpm);
    }

    public void setPdaText(String pda){
        pdaText.setText(pda);
    }

    private ObdMainService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ObdMainService.LocalBinder) service).getService();
            mBoundService.setActivity(CustomBluetoothActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };
    private boolean mIsBound;

    void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        if(!mIsBound) {
            bindService(new Intent(this, ObdMainService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
            Log.d("DBZ","Bounded");
        }
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            Log.d("DBZ","unBounded");
        }
    }
}