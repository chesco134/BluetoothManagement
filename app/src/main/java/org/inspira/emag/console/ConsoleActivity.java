package org.inspira.emag.console;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.capiz.bluetooth.R;
import org.inspira.emag.bluetooth.BluetoothManager;
import org.inspira.emag.bluetooth.DevicePickerActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ConsoleActivity extends AppCompatActivity {

    private static final int START_CLIENT_ACTION = 45;
    private boolean isRunning;
    private BluetoothSocket btSocket;
    private DataOutputStream salida;
    private DataInputStream entrada;
    private BluetoothDevice rmDev;
    private EditText inputCommand;
    private TextView logZone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputCommand = (EditText)findViewById(R.id.fragment_main_input_command);
        logZone = (TextView)findViewById(R.id.fragment_main_log_zone);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            String cmd;

            @Override
            public void onClick(View view) {
                if (inputCommand.isEnabled())
                    try {
                        cmd = inputCommand.getText().toString();
                        salida.write((cmd + "\r").getBytes());
                        logZone.append("\n\t---> " + cmd + "\n\tResponse -> ");
                        inputCommand.setText("");
                    } catch (IOException e) {
                        e.printStackTrace();
                        isRunning = false;
                        inputCommand.setEnabled(false);
                        makeSnackbar("Problemas con la conexión, vuelva a conectar");
                    }
                else
                    makeSnackbar("Debemos conectarnos primero");
            }
        });
        if(savedInstanceState == null){
            isRunning = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, DevicePickerActivity.class);
            startActivityForResult(i, START_CLIENT_ACTION);
            return true;
        }else if(id == R.id.action_clear){
            logZone.setText("");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("logZone", logZone.getText().toString());
        outState.putBoolean("isRunning",isRunning);
        if(rmDev != null)
            outState.putString("remote_device",rmDev.getAddress());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        logZone.setText(savedInstanceState.getString("logZone"));
        isRunning = savedInstanceState.getBoolean("isRunning");
        inputCommand.setEnabled(isRunning);
        if(isRunning) {
            rmDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(savedInstanceState.getString("remote_device"));
            new PerformConnection().execute();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if( isRunning )
        try{
            btSocket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case START_CLIENT_ACTION:
                    rmDev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(data.getExtras().getString("device_addr"));
                    new PerformConnection().execute();
                    Snackbar.make(logZone, "Conectando...", Snackbar.LENGTH_SHORT).setAction("Connecting",null).show();
                    break;
                default:
            }
        }
    }

    private void makeSnackbar(String message){
        Snackbar.make(inputCommand,message, Snackbar.LENGTH_SHORT)
                .setAction("Aviso",null).show();
    }

    private class PerformConnection extends AsyncTask<String,String,String> {

        @Override
        protected String doInBackground(String... args){
            String tl = null;
            BluetoothSocket temp;
            try{
                btSocket = rmDev.createRfcommSocketToServiceRecord(UUID.fromString(BluetoothManager.MY_UUID));
                btSocket.connect();
                tl = "Success";
            }catch(IOException e){
                Log.e("Tulman", "There was an error while establishing Bluetooth connection. Falling back..", e);
                Class<?> clazz = btSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                try {
                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};
                    temp = (BluetoothSocket) m.invoke(btSocket.getRemoteDevice(), params);
                    temp.connect();
                    btSocket = temp;
                    tl = "Success";
                } catch (Exception e2) {
                    Log.e("TulmanSan", "Couldn't fallback while establishing Bluetooth connection. Stopping app..", e2);
                }
                e.printStackTrace();
            }finally{
                try{
                    salida = new DataOutputStream(btSocket.getOutputStream());
                    entrada = new DataInputStream(btSocket.getInputStream());
                }catch(NullPointerException | IOException e){
                    Log.d("Trankos","Pos no pudimos crear los canales tú u.u");
                    tl = null;
                }
            }
            return tl;
        }

        @Override
        public void onPostExecute(String result){
            if(result != null){
                isRunning = true;
                inputCommand.setEnabled(true);
                new InputListener().execute();
                makeSnackbar("Conectados");
            }else{
                isRunning = false;
                inputCommand.setEnabled(false);
                makeSnackbar("Error al conectar");
            }
        }
    }

    private class InputListener extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... args){
            String result = null;
            try{
                char c;
                while(true){
                    if((c = (char)entrada.read()) != '>')
                        publishProgress("" + c);
                    else
                        publishProgress("*");
                }
            }catch(IOException e){
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(String... params){
            logZone.append(params[0]);
        }

        @Override
        protected void onPostExecute(String result){
            makeSnackbar("Perdimos la conexión");
            isRunning = false;
            inputCommand.setEnabled(false);
        }
    }
}