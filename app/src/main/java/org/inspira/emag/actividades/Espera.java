package org.inspira.emag.actividades;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.capiz.bluetooth.R;

import java.util.Timer;
import java.util.TimerTask;

public class Espera extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waiting_for_participant);
		if(savedInstanceState == null)
		try{
			((TextView)findViewById(R.id.message)).setText(getIntent().getExtras().getString("message"));
		}catch(NullPointerException e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putString("message",((TextView)findViewById(R.id.message)).getText().toString());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		((TextView)findViewById(R.id.message)).setText(savedInstanceState.getString("message"));
	}
}
