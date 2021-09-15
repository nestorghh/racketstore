package com.caspr.android.racketstore;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.caspr.android.racketstore.Questions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MCQuizActivity extends AppCompatActivity {


    Button b_next;
    TextView tv_question;
    RadioGroup radioGroup;
    android.support.v7.widget.Toolbar toolbar;

    List<String> mcquestions;
    int curQuestion = 0;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mc_question);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Questionnaire");

        b_next = (Button) findViewById(R.id.next);
        tv_question = (TextView) findViewById(R.id.question);
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        b_next.setVisibility(View.INVISIBLE);

        mcquestions = new ArrayList<>();
        mcquestions.addAll(Arrays.asList(Questions.mcquestions));

        //b_next.setVisibility(View.INVISIBLE);
        tv_question.setText(mcquestions.get(curQuestion));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton checkedRadioButton = (RadioButton)radioGroup.findViewById(i);
                i = radioGroup.indexOfChild(checkedRadioButton);
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                SharedPreferences.Editor edt = pref.edit();
                edt.putBoolean("activity_executed_mcq_"+curQuestion, true);
                edt.putInt("mcq_"+curQuestion, i);
                edt.commit();
                b_next.setVisibility(View.VISIBLE);
            }
        });

        b_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                if (curQuestion < 1){
                    //save answer to shared preferences here
                    curQuestion++;
                    tv_question.setText(mcquestions.get(curQuestion));
                    radioGroup.clearCheck();
                    b_next.setVisibility(View.INVISIBLE);
                    //et_answer.setText("");
                } else if (pref.getInt("mcq_1",-1)==1 & curQuestion< Questions.mcquestions.length-1){
                    curQuestion++;
                    tv_question.setText(mcquestions.get(curQuestion));
                    radioGroup.clearCheck();
                    b_next.setVisibility(View.INVISIBLE);
                }
                else{
                    int q0 = pref.getInt("mcq_0",-1);
                    int q1 = pref.getInt("mcq_1",-1);
                    Log.e("mcq1",String.valueOf(q0));
                    Log.e("mcq2",String.valueOf(q1));
                    Intent intent = new Intent(getApplicationContext(), QuizActivity.class);
                    startActivity(intent);
                }

            }
        });

    }

    @Override
    public void onBackPressed(){
    }

}