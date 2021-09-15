package com.caspr.android.racketstore;


import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Accounts {

    public static int count;

    public static JSONObject getAccounts(Context mContext) throws JSONException {
        count=0;
        JSONArray jsonArr = new JSONArray();
        JSONObject emailObj = new JSONObject();
        //GET_ACCOUNTS
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            emailObj.put("count",count);

            JSONObject elem = new JSONObject();
            elem.put("name", "Account permission not granted");
            elem.put("type", "Error");
            jsonArr.put(elem);

            emailObj.put("list",jsonArr);
            return emailObj;
        }

        try{
            Account[] accounts = AccountManager.get(mContext).getAccounts();
            //Account[] accounts = AccountManager.get(mContext).getAccountsByType(null);
            for (Account account : accounts) {
                //possibleEmail += " "+account.name+" : "+account.type+" \n";

                JSONObject elem = new JSONObject();
                elem.put("name", account.name);
                elem.put("type", account.type);
                jsonArr.put(elem);
                //possibleEmail += " "+account.name+" \n";
                count++;

            }
            emailObj.put("count",count);
            if (count == 0){
                JSONObject elem = new JSONObject();
                elem.put("name", "No accounts found.");
                elem.put("type", "Empty");
                jsonArr.put(elem);
            }
            emailObj.put("list", jsonArr);

        }
        catch(Exception e)
        {
            emailObj.put("count",count);

            JSONObject elem = new JSONObject();
            elem.put("name", e.toString());
            elem.put("type", "Exception");
            jsonArr.put(elem);

            emailObj.put("list",jsonArr);
            return emailObj;
            //Log.i("Exception", "Exception:"+e) ;
            //Toast.makeText(this, "Exception:"+e, Toast.LENGTH_SHORT).show();
        }

        // Show on screen
        //Toast.makeText(this, possibleEmail, Toast.LENGTH_SHORT).show();

        //possibleEmail = "\n Account Count: "+count+"\n"+possibleEmail;

        return emailObj;
    }
}
