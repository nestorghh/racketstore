package com.caspr.android.racketstore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Random;

public class SplashActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //setContentView(R.layout.activity_splash);

// decide here whether to navigate to Login or Main Activity
        SharedPreferences pref = this.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if (pref.getBoolean("activity_executed", false)) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // show this when you just open the app for the first time
            //Intent intent = new Intent(this, Consent.class);
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
