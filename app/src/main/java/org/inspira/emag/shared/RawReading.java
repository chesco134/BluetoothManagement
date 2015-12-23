package org.inspira.emag.shared;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jcapiz on 27/11/15.
 */
public class RawReading implements Shareable{

    private JSONObject json;

    public RawReading(String content){
        try{
            json = new JSONObject();
            json.put("content",content);
            json.put("idViaje","-1");
        }catch(JSONException e){
            Log.e("RawReading", e.getMessage());
        }
    }

    public String rawMessage(){
        return json.toString();
    }

    public JSONObject getJson(){
        return json;
    }

    @Override
    public void commitEntry(Context ctx) {

    }
}
