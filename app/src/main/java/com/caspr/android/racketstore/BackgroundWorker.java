package com.caspr.android.racketstore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class BackgroundWorker extends AsyncTask<String,Void,String> {
    Context context;
    AlertDialog alertDialog;

    BackgroundWorker(Context ctx){
        context=ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        //String user_name = params[1];
        String password = params[1];
        String type = params[0];
        String login_url = "https://www.monkeyrocket.review/login.php";
        if (type.equals("login")){
            try {
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                //String post_data = URLEncoder.encode("user_name","UTF-8")+"="+URLEncoder.encode(user_name,"UTF-8")+"&"
                 //       +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");

                String post_data = URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");

                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

                String result="";
                String line="";
                while((line=bufferedReader.readLine())!=null){
                    result+=line;
                }
                bufferedReader.close();
                inputStream.close();
                int responseCode = httpURLConnection.getResponseCode();
                Log.i("server response login: ",  Integer.toString(responseCode));
                httpURLConnection.disconnect();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("We are login you in!");
    }

    @Override
    protected void onPostExecute(String result) {
        alertDialog.setTitle("Login Status");
        alertDialog.setMessage(result);
        alertDialog.show();

        super.onPostExecute(result);
        Log.i("server_login_response: ", result);

        if(result!=null && result.equals("You are logged in! Welcome to RacketStore!")){
            //register that user already input racket code.
             SharedPreferences pref = context.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
            SharedPreferences.Editor edt = pref.edit();
            // Only set this when first snapshot has been recorded.
            // edt.putBoolean("activity_executed", true);

            //edt.putString("workerid", RegistrationCode.getEmail());
            edt.putString("workerid", RegistrationCode.getCodeReg());
            edt.commit();
            //Intent intent = new Intent(context, MainActivity.class);
            Intent intent = new Intent(context, MCQuizActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
