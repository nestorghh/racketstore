package com.caspr.android.racketstore;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//import com.android_examples.getinstalledappiconname_android_examplescom.R;

public class RegistrationCode extends AppCompatActivity {

    private static String emailReg;
    private static String codeReg;

    EditText emailInput;
    EditText regCode;

    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_code);
        submitButton = (Button) findViewById(R.id.submitbutton);

        //emailInput = (EditText) findViewById(R.id.emailinput);
        regCode = (EditText)  findViewById(R.id.code);

        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //emailReg = emailInput.getText().toString();
                codeReg = regCode.getText().toString();

                //showToast(emailReg);
                //showToast(String.valueOf(codeReg));

                onLogin();

                //register that user already input racket code.
                //SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                //SharedPreferences.Editor edt = pref.edit();
                //edt.putBoolean("activity_executed", true);
                //edt.commit();

                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //startActivity(intent);
                //finish();
            }
        });
    }

    public void onLogin(){
        //String emailReg = emailInput.getText().toString();
        String codeReg = regCode.getText().toString();
        String type = "login";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        //backgroundWorker.execute(type,emailReg,codeReg);
        backgroundWorker.execute(type,codeReg);
    }


    /*public static String getEmail(){
        return emailReg;
    }*/

    public static String getCodeReg(){
        return codeReg;
    }

    private void showToast(String text){
        Toast.makeText(RegistrationCode.this, text, Toast.LENGTH_SHORT).show();
    }

    //@Override
    //public void onBackPressed(){
    //}


}
