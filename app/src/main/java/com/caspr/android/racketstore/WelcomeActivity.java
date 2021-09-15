package com.caspr.android.racketstore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WelcomeActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //generate random id
        Random rnd = new Random();
        int randomId = 1000000000 + rnd.nextInt(90000000);


        SharedPreferences pref = this.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = pref.edit();
        edt.putBoolean("activity_executed_randomId", true);
        edt.putInt("randomId", randomId);
        edt.commit();

        setContentView(R.layout.welcome);

        Button nextbutton = (Button) findViewById(R.id.welcomeAcceptBtn);

        nextbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Consent.class);
                startActivity(intent);
                //finish();
            }
        });


    }


}
