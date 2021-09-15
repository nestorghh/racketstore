package com.caspr.android.racketstore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.caspr.android.racketstore.Questions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

public class QuizActivity extends AppCompatActivity {


    Button b_next;
    TextView tv_question;
    EditText et_answer;
    android.support.v7.widget.Toolbar toolbar;

    List<String> questions;
    int curQuestion = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.question);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Questionnaire");

        b_next = (Button) findViewById(R.id.next);
        tv_question = (TextView) findViewById(R.id.question);
        et_answer = (EditText) findViewById(R.id.answer);

        b_next.setVisibility(View.INVISIBLE);

        questions = new ArrayList<>();
        questions.addAll(Arrays.asList(Questions.questions));


        tv_question.setText(questions.get(curQuestion));


        et_answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //b_next.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor edt = pref.edit();
                edt.putBoolean("activity_executed_oq_"+curQuestion, true);
                edt.putString("oq_"+curQuestion, editable.toString());
                edt.commit();
                b_next.setVisibility(View.VISIBLE);

            }
        });

        b_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                if (curQuestion < Questions.questions.length-1){

                    curQuestion++;
                    tv_question.setText(questions.get(curQuestion));
                    et_answer.setText("");
                    b_next.setVisibility(View.INVISIBLE);
                } else{
                    String q0 = pref.getString("oq_0","none");
                    String q1 = pref.getString("oq_1","none");
                    String q2 = pref.getString("oq_2","none");
                    String q3 = pref.getString("oq_3","none");
                    Log.e("oq0",String.valueOf(q0));
                    Log.e("oq1",String.valueOf(q1));
                    Log.e("oq2",String.valueOf(q2));
                    Log.e("oq3",String.valueOf(q3));
                    // I have added this on sep 13
                    SharedPreferences.Editor edt = pref.edit();
                    edt.putBoolean("activity_executed_questionnaire", true);

                    // I had to create a new thread since it was giving me an error.
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            send_meta();
                        }
                    });

                    thread.start();

                    //register that user already input racket code.
                    //SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                    //SharedPreferences.Editor edt = pref.edit();

                    edt.putBoolean("activity_executed", true);
                    edt.commit();

                    addialog();

                    //Intent intent = new Intent(getApplicationContext(), RegistrationCode.class);
                    //startActivity(intent);
                }
        }
        });

    }

    @Override
    public void onBackPressed(){
    }

    protected void addialog() {
        CharSequence options[] = new CharSequence[]{"Contacts: Accounts registered on the device, and phone number information", "Device ID: Device information like model and manufacturer", "Foreground App: App on screen","Installed Apps: Packages installed on the device"};
        View view = LayoutInflater.from(this).inflate(R.layout.alertdialog_layout, null);
        ListView listView = view.findViewById(R.id.list_view);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_dialog, R.id.tv1, options){};
        listView.setAdapter(arrayAdapter);
        final AlertDialog dialog = new AlertDialog.Builder(this,R.style.AlertDialogTheme)
                .setTitle("RacketStore needs access to")
                .setMessage("RacketStore periodically captures the following:")
                .setView(view)
                .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Intent intent = new Intent(getApplicationContext(), RegistrationCode.class);
                        //Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
                        //Intent intent = new Intent(getApplicationContext(), RegistrationCode.class);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        //startActivity(intent);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();

        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });


        dialog.show();
    }

    public void send_meta(){

        JSONObject snapshotJsonObj = new JSONObject();

        SharedPreferences pref2 = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);

        String user = pref2.getString("workerid", "");

        int mcq0 = pref2.getInt("mcq_0",-1);
        int mcq1 = pref2.getInt("mcq_1",-1);
        int mcq2 = pref2.getInt("mcq_2",-1);

        String oq0 = pref2.getString("oq_0","none");
        String oq1 = pref2.getString("oq_1","none");
        String oq2 = pref2.getString("oq_2","none");

        int randomId = pref2.getInt("randomId",-1);
        //long currentTime = new Date().getTime();
        long currentTime = System.currentTimeMillis();

        try {
            snapshotJsonObj.put("timestamp_ms", currentTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String tZone = TimeZone.getDefault().getID();

        try {
            snapshotJsonObj.put("timezone", tZone);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try{
            snapshotJsonObj.put("randomId",randomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("user_name",user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("written_reviews?",mcq0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("written_paid_reviews?",mcq1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("written_paid_reviews_device?",mcq2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("user_accounts",oq0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("access_devices",oq1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            snapshotJsonObj.put("days_installed",oq2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            snapshotJsonObj.put("dev_info", getDevInfo(this));
        } catch (JSONException e){
            e.printStackTrace();
        }

        String s = snapshotJsonObj.toString();
        String slow_filename = "slow_snapshot";
        append_snapshot(s, slow_filename);

        // Send immediately to server not needed because we accumulate above
/*        try {
            MyTestService.log_to_server(s);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    public void append_snapshot(String json_snapshot, String filename){
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(filename, Context.MODE_APPEND);
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

    public JSONObject getDevInfo(Context mContext) throws JSONException {

        //JSONArray jsonArr = new JSONArray();
        JSONObject devObj = new JSONObject();
        TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        devObj.put("phone_type",  tm.getPhoneType());

        if (tm.getPhoneType()==1){
            boolean t=tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT;
            devObj.put("sim_absent", String.valueOf(t));
        }

        int currentVer = android.os.Build.VERSION.SDK_INT;
        devObj.put("android_api",  currentVer);

        devObj.put("serial",  android.os.Build.SERIAL);
        devObj.put("model", Build.MODEL);
        devObj.put("brand", Build.BRAND);
        devObj.put("manufacturer", Build.MANUFACTURER);
        devObj.put("design", Build.DEVICE);
        devObj.put("product", Build.PRODUCT);
        devObj.put("is_emulator", isEmulator());

        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        devObj.put("androidID", androidId);

        return devObj;
    }

    private String isEmulator() {
        boolean res;

        try{
            res = (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
            Log.e("emulator_test result:",String.valueOf(res));
        } catch (Exception e){
            Log.e("emulator_test","Exception during testing");
            return "false";
        }

        return String.valueOf(res);
    }


}
