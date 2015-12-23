package org.inspira.emag.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

/**
 * Created by jcapiz on 14/09/15.
 */
public class BluetoothManager {

    public static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 102;
    private Activity activity;
    private ArrayAdapter<String> mArrayAdapter;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	
    	@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("LAWAFOMECULIAOWEON123: ","Dispositivo descubierto: " + device.getName());
                // Add the name and address to an array adapter to show in a ListView
                try{
                    DevicePickerActivity a = (DevicePickerActivity)activity;
                    a.addDevice(device.getName() + "\n" + device.getAddress());
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
            }
        }
    };
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);


    public BluetoothManager(Activity activity){
        this.activity = activity;
        mArrayAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_list_item_1);
    }

    public ArrayAdapter<String> getmArrayAdapter(){
        return mArrayAdapter;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter;
    }

    public void enableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public void justTryToEnableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public boolean isBluetoothEnabled(){
        return mBluetoothAdapter.isEnabled();
    }

    public void loadPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private void logSomething(String toBeLogged){
        Log.d("BluetoothManager: ", toBeLogged);
    }

    public void registerReceiver(){
        activity.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        mBluetoothAdapter.startDiscovery();
        logSomething("Iniciando descubrimiento de dispositivos.");
    }

    public void unregisterReceiver(){
        activity.unregisterReceiver(mReceiver);
    }

    public void cancelDiscovery(){
    	try{ mBluetoothAdapter.cancelDiscovery(); } catch(NullPointerException e){ e.printStackTrace(); }
    }
}