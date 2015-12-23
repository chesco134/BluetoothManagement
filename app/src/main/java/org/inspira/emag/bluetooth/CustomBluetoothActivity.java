package org.inspira.emag.bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.capiz.bluetooth.R;
import org.inspira.emag.console.ConsoleActivity;
import org.inspira.emag.service.ObdMainService;

/**
 * Created by jcapiz on 15/09/15.
 */
public class CustomBluetoothActivity extends AppCompatActivity {

	private static final int START_CLIENT_ACTION = 2;
	private Button clientMode;
	private TextView buttonLabel;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView speedText;
    private TextView rpmText;
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
		manager = new BluetoothManager(this);
		mService = new Intent(this, ObdMainService.class);
        ((TextView)findViewById(R.id.welcome)).setTypeface(Typeface.createFromAsset(getAssets(), "RobotoCondensed/RobotoCondensed-Regular.ttf"));
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
                        Snackbar.make(clientMode, "Lectura de datos detenida", Snackbar.LENGTH_SHORT)
                                .setAction("Aviso", null).show();
                        serviceOn = !serviceOn;
                    }
                }
            });
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_activity_menu, menu);
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
        }
        return true;
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
        outState.putString("speed",speedText.getText().toString());
        outState.putString("rpm",rpmText.getText().toString());
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
                Snackbar.make(clientMode,"Lectura de datos detenida",Snackbar.LENGTH_SHORT)
                        .setAction("Aviso", null).show();
            }else{
                Toast.makeText(this,"Presione una vez más para salir", Toast.LENGTH_SHORT).show();
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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        mService.putExtra("device_addr", data.getStringExtra("device_addr"));
                    } else {
                        mService.putExtra("device_addr", data.getStringExtra("device_addr"));
                    }
                    clientMode.setBackgroundResource(R.drawable.on_button);
                    buttonLabel.setText(getResources().getString(R.string.detener_lectura_datos));
                    Snackbar.make(clientMode, "Lectura de datos iniciada", Snackbar.LENGTH_SHORT)
                            .setAction("Aviso", null).show();
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

    private ObdMainService mBoundService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((ObdMainService.LocalBinder) service).getService();
            mBoundService.setActivity(CustomBluetoothActivity.this);
        }

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
        }
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
}