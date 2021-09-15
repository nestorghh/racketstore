package com.caspr.android.racketstore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class ShutdownReceiver extends BroadcastReceiver {

    public void append_snapshot(String json_snapshot, String filename, Context context){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(filename, Context.MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write((json_snapshot+"\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        //save the date here
        SharedPreferences sharedpreferences;
        String mypreference = "reboot";
        String date = "dateKey";
        sharedpreferences = context.getSharedPreferences(mypreference, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Date currentTime = Calendar.getInstance().getTime();
        editor.putString(date, currentTime.toString());
        editor.commit();

        long currentTime2 = new Date().getTime();
        long timestamp = System.currentTimeMillis();

        SharedPreferences pref2 = context.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        int randomId = pref2.getInt("randomId",-1);
        String user = pref2.getString("workerid", "");

        JSONObject snapshotJsonObj = new JSONObject();

        try {
            snapshotJsonObj.put("timestamp", timestamp);
            snapshotJsonObj.put("randomId", randomId);
            snapshotJsonObj.put("user_name",user);
            snapshotJsonObj.put("reboot", String.valueOf(currentTime2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String filename = "snapshot";

        // Append new snapshot
       append_snapshot(String.valueOf(snapshotJsonObj), filename, context);


    }
}