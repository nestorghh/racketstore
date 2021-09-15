package com.caspr.android.racketstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


//import com.android_examples.getinstalledappiconname_android_examplescom.R;

public class Consent extends Activity {
    android.support.v7.widget.Toolbar toolbar;
    public static String country="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consent_pp);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Privacy Policy");
        Button agreebutton = (Button) findViewById(R.id.agree);
        Button disagreebutton = (Button) findViewById(R.id.disagree);

        WebView wv;
        wv=(WebView)findViewById(R.id.webView1);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.loadUrl("https://www.monkeyrocket.review/PRIVACY_POLICY_MR.html");

        agreebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //addialog();
                // I have added this to the addialog method instead.14
                //Intent intent = new Intent(getApplicationContext(), MCQuizActivity.class);
                Intent intent = new Intent(getApplicationContext(), RegistrationCode.class);
                startActivity(intent);
                finish();

            }
        });
        disagreebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                exitToHome();
            }
        });

    }
    public void exitToHome(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

/*
    protected void addialog() {
        CharSequence options[] = new CharSequence[]{"Contacts", "Device ID", "Foreground App","Installed Apps"};
        View view = LayoutInflater.from(this).inflate(R.layout.alertdialog_layout, null);
        ListView listView = view.findViewById(R.id.list_view);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_dialog, R.id.tv1, options){};
        listView.setAdapter(arrayAdapter);
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("RacketStore needs access to:")
                .setMessage("RacketStore needs access to:")
                .setView(view)
                .setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Intent intent = new Intent(getApplicationContext(), RegistrationCode.class);
                        //Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
                        Intent intent = new Intent(getApplicationContext(), MCQuizActivity.class);
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
*/






}