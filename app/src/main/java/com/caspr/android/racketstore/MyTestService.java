package com.caspr.android.racketstore;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.JobIntentService;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MyTestService extends JobIntentService {
    //public MyTestService() {
    //    super("MyTestService");
    //}

    private static final int JOB_ID = 1000;

    public static void enqueueWork(Context ctx, Intent intent) {
        enqueueWork(ctx, MyTestService.class, JOB_ID, intent);
    }

    public void rotate_file(String filename, String extension) throws IOException {
        // compress file

        File directory;
        directory = getFilesDir();
        File filename_path = new File(directory.getPath() +"/"+ filename);

        Log.i("handler_file_threshold", String.valueOf(filename_path)+", size: "+String.valueOf(filename_path.length()));

        File new_filename_path = new File(directory.getPath()+"/_"+filename);
        filename_path.renameTo(new_filename_path);


        filename = "_"+filename;

        String timestamp = String.valueOf(System.currentTimeMillis());
        File new_file = new File(directory.getPath()+"/"+timestamp+filename+"."+extension);
        FileOutputStream new_filename = new FileOutputStream(new_file);
        ZipOutputStream zipOut = new ZipOutputStream(new_filename);
        File fileToZip = new File(directory.getPath()+"/"+filename);
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fileToZip.delete();
        zipOut.close();
        fis.close();
        new_filename.close();
    }

    public String create_snapshot() throws JSONException {
        JSONObject json_foreground = new JSONObject();
        //Log.i("handler","Created every 1 sec");
        String screenOn = isScreenOn();
        long timestamp = System.currentTimeMillis();

        int batteryLevel = getBatteryPercentage();
        JSONObject ramInfo = getRam();

        SharedPreferences pref2 = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        int randomId = pref2.getInt("randomId",-1);

        //String valdate = sharedpreferences.getString(date,"");
        String user = pref2.getString("workerid", "");

        JSONArray[] appsSet = handleApps();
        //JSONArray addedSetJson = appsSet[0];
        JSONArray addedSetJson = appsSet[2];
        JSONArray removedSetJson = appsSet[1];
        JSONArray addedSetJsonUpdate = appsSet[3];

        try {
            json_foreground.put("battery_level", batteryLevel);
            json_foreground.put("randomId", randomId);
            json_foreground.put("timestamp", timestamp);
            json_foreground.put("screen_on",screenOn);
            json_foreground.put("dev_ram", ramInfo);
            json_foreground.put("user_name",user);
            if (addedSetJson.length() != 0){
                json_foreground.put("added_apps", addedSetJson);
            }
            if (removedSetJson.length() != 0){
                json_foreground.put("removed_apps", removedSetJson);
            }
            if (addedSetJsonUpdate.length() != 0){
                json_foreground.put("updated_apps", addedSetJsonUpdate);
            }
            // Bogdan check
            //if (screenOn == "true") {
            String foreground_package = getTopPackageName();
            json_foreground.put("foreground_package", foreground_package);
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.valueOf(json_foreground);
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

    public void do_work(){
        final int threshold_size = 100000;

        String json_snapshot = null;
        try {
            json_snapshot = create_snapshot();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String filename = "snapshot";

        File directory = getFilesDir();
        File filename_path = new File(directory.getPath() +"/"+ filename);
        if (filename_path.length() > threshold_size){
            // compress file
            try {
                rotate_file(filename, "zip");
            } catch (IOException e) {
                Log.i("handler", "Couldn't rotate file");
                e.printStackTrace();
            }
        }

        // Append new snapshot
        append_snapshot(json_snapshot, filename);

/*                File[] files = directory.listFiles();

                Log.i("handler", String.valueOf(json_snapshot));
                Log.i("handler_dir", String.valueOf(directory));
                for (File myfile:files) {
                    Log.i("handler_file", String.valueOf(myfile)+", size: "+String.valueOf(myfile.length()));
                }*/
        Log.i("handler", String.valueOf(json_snapshot));
    }

    // add functions here to do installed apps

    public Set<String> getInstalledPref(){
        SharedPreferences pref = this.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        Set<String> set2 = pref.getStringSet("installedApps", new HashSet<String>());
        return set2;
    }

    public Set<String> getInstalledPrefUpdate(){
        SharedPreferences pref = this.getSharedPreferences("ActivityPREF2", Context.MODE_PRIVATE);
        Set<String> set3 = pref.getStringSet("installedAppsUpdate", new HashSet<String>());
        return set3;
    }


    public JSONArray[] handleApps() throws JSONException {

        SharedPreferences pref = this.getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = pref.edit();

        Set<String> sharedSet = getInstalledPref();
        Log.d("installed beginning",String.valueOf(sharedSet));

        SharedPreferences pref2 = this.getSharedPreferences("ActivityPREF2", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt2 = pref2.edit();


        Set<String> sharedSetUpdate = getInstalledPrefUpdate();
        Log.d("installedUpdate",String.valueOf(sharedSetUpdate));


        List<List<String>> installedPackages = MainActivity.getInstalledPackageNames(this);
        Set<String> currentSet = new HashSet<String>();
        currentSet.addAll(installedPackages.get(0));

        Set<String> currentSetUpdate = new HashSet<String>();
        currentSetUpdate.addAll(installedPackages.get(1));


        // COPY EACH ARRAY

        Set<String> addedSet = new HashSet<String>(currentSet);
        Set<String> removeSet = new HashSet<String>(sharedSet);

        Set<String> addedSetUpdate = new HashSet<String>(currentSetUpdate);


        addedSet.removeAll(sharedSet);
        Log.d("installed added",String.valueOf(addedSet));
        removeSet.removeAll(currentSet);
        Log.d("installed removed",String.valueOf(removeSet));

        addedSetUpdate.removeAll(sharedSetUpdate);
        Log.d("installed Updated",String.valueOf(addedSetUpdate));


        if (!(addedSet.isEmpty() && removeSet.isEmpty())){
            Log.d("installed 2", "actualizar el shared pref and cset");
            edt.putStringSet("installedApps", currentSet);
            edt.commit();

        }

        if (!(addedSetUpdate.isEmpty())){
            Log.d("installed update", "actualizar el shared pref and cset update");
            edt2.putStringSet("installedAppsUpdate", currentSetUpdate);
            edt2.commit();

        }

        // convert addedset and removeset to jsonarray

        JSONArray addedSetJson = new JSONArray(addedSet);
        JSONArray removeSetJson = new JSONArray(removeSet);
        JSONObject appsJsonObj = new JSONObject();
        //String appList="";

        //JSONArray addedSetJsonUpdate = new JSONArray(addedSetUpdate);

        //call the function getPackageInformation for the added apps

        JSONArray jsonArr = new JSONArray();
        for (String pack: addedSet){
            jsonArr.put(MainActivity.getPackageInformation(this,pack));
            //Log.i("apkNameAdded",MainActivity.getApkName(this,pack));
        }
        Log.i("added_info",jsonArr.toString());

        JSONArray jsonArrUpdate = new JSONArray();
        for (String pack: addedSetUpdate){
            String packName = pack.split("\\|")[0];
            Log.d("packname",packName);
            //check if app is in installed set and then not capture
            if (!(addedSet.contains(packName))){
                jsonArrUpdate.put(MainActivity.getPackageInformation(this,packName));
            }

        }

        Log.i("added_info_update",jsonArrUpdate.toString());

        JSONArray res[] = new JSONArray[4];
        //appList = "\n App Count: "+count+"\n"+appList;
        res[0] = addedSetJson;
        res[1] = removeSetJson;
        res[2] = jsonArr;
        res[3] = jsonArrUpdate;
        return res;
    }




    @Override
    protected void onHandleWork(Intent intent) {
        // Do the task here

        SharedPreferences sharedpreferences;
       /* String mypreference = "reboot";
        String date = "dateKey";*/

        JSONObject snapshotJsonObj = new JSONObject();

        /*sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);*/
        SharedPreferences pref2 = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);

        //String valdate = sharedpreferences.getString(date,"");
        String user = pref2.getString("workerid", "");

        String filename = "snapshot";

        File snap_dir = getFilesDir();
        File filename_path = new File(snap_dir.getPath() +"/"+ filename);

        long last_modified_time = filename_path.lastModified();
        long current_time = System.currentTimeMillis();

        if (current_time - last_modified_time > 90000) {
            final Handler handler = new Handler(Looper.getMainLooper());
            final int delay = 1000; //milliseconds

            handler.postDelayed(new Runnable(){
                public void run(){
                    do_work();
                    handler.postDelayed(this, delay);

                }
            }, delay);
        }

        /*int mcq0 = pref2.getInt("mcq_0",-1);
        int mcq1 = pref2.getInt("mcq_1",-1);
        int mcq2 = pref2.getInt("mcq_2",-1);

        String oq0 = pref2.getString("oq_0","none");
        String oq1 = pref2.getString("oq_1","none");
        String oq2 = pref2.getString("oq_2","none");

*/
        int randomId = pref2.getInt("randomId",-1);

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

     /*   try{
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
        }*/

        // Already collected on receiver to snapshot file
        /*try {
            snapshotJsonObj.put("time_reboot",valdate);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //long currentTime = new Date().getTime();
        long currentTime = System.currentTimeMillis();
        //Log.i("MyTestService at "+currentTime, "Service running");
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


        //Log.i("battery", batteryLevelJsonObj.toString());
        // it is being collected every second on the handler
      /*  try {
            snapshotJsonObj.put("battery_level", getBatteryPercentage());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        //Log.i("Foreground App", foregroundApp());
        //Log.i("Foreground App", getTopPackageName());
        // it is being collected on the handler every second
        /*try {
            snapshotJsonObj.put("foreground_app", getTopPackageName());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //Log.v("Save mode on?",""+isInSavedMode());
        try {
            snapshotJsonObj.put("power_save_mode", isInSavedMode());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // double check if we want to keep this (min sdk is 23 for this to work)
        //Log.v("is Idle?",""+isIdle());
      /*  try {
            snapshotJsonObj.put("is_idle",isIdle());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //Log.v("Screen on?",""+isScreenOn());
        // it is being collected on the handler every second
     /*   try {
            snapshotJsonObj.put("screen_on", isScreenOn());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        // new code , February 20th
      /*  try {
            snapshotJsonObj.put("installed_apps", MainActivity.getValidPackages(this));
        } catch (JSONException e){
            e.printStackTrace();
        }*/

        try {
            snapshotJsonObj.put("dev_info", getDevInfo(this));
        } catch (JSONException e){
            e.printStackTrace();
        }

      /*  try {
            snapshotJsonObj.put("dev_ram", getRam());
        } catch (JSONException e){
            e.printStackTrace();
        }*/

        try {
            snapshotJsonObj.put("dev_storage", Storage.getDevStorage(this));
        } catch (JSONException e){
            e.printStackTrace();
        }

        try {
            snapshotJsonObj.put("accounts", Accounts.getAccounts(this));
        } catch (JSONException e){
            e.printStackTrace();
        }

/*        try {
            JSONObject installedAppsJsonObj = installedApps();
            snapshotJsonObj.put("installed_apps", installedAppsJsonObj);
            //Log.i("Installed apps", installedAppsJsonObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //Log.i("Running apps", runningApps(this));

        //Log.i("Usage Stats", showUsageStats());
        //Log.d("usage list", statsApps().toString());

      /*  try {
            snapshotJsonObj.put("usage_stats", showUsageStats());
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        try {
            snapshotJsonObj.put("stopped_apps", getStoppedApps(this));
        }catch (JSONException e){
            e.printStackTrace();
        }

        try {
            snapshotJsonObj.put("suspended_apps", getSuspendedApps(this));
        }catch (JSONException e){
            e.printStackTrace();
        }

        final int chunksize = 2048;
//        String s = showUsageStats();
        String s = snapshotJsonObj.toString();
        //String s = getStoppedApps(this);

        try {
            int threshold_size = 8000;
            String slow_filename = "slow_snapshot";

            File directory = getFilesDir();
            File slow_filename_path = new File(directory.getPath() +"/"+ slow_filename);
            if (slow_filename_path.length() > threshold_size){
                // compress file
                try {
                    rotate_file(slow_filename, "sip");
                } catch (IOException e) {
                    Log.i("handler_sip", "Couldn't rotate file");
                    e.printStackTrace();
                }
            }

            // Append new snapshot
            append_snapshot(s, slow_filename);

            // Send to server immediately.
            //log_to_server(s);

            //File directory = getFilesDir();
            File[] files = directory.listFiles();
            for (File myfile:files) {
                String fname = myfile.getName();
                String outname = fname.substring(fname.length() - 3);
                Log.i("fname", fname+"- last 3 - " + outname);

                if(outname.equals("zip")){
                    Log.i("fname_true", fname+ " - sending size: " +String.valueOf(myfile.length()));
                    log_to_server_zip(myfile, "zip");
                } else if(outname.equals("sip")){
                    Log.i("fname_true_slow", fname+ " - sending size: " +String.valueOf(myfile.length()));
                    log_to_server_zip(myfile, "sip");
                }
                Log.i("handler_file_slow", String.valueOf(myfile)+", size: "+String.valueOf(myfile.length()));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        final int chunkSize = 2048;
        for (int i = 0; i < s.length(); i += chunkSize) {
//            Log.d("Usage Stats", s.substring(i, Math.min(s.length(), i + chunkSize)));
            Log.i("snapshot data ", s.substring(i, Math.min(s.length(), i + chunkSize)));
        }



    }

///////////////////////// Old send immediately to server.
//    public static void log_to_server(String s) throws IOException {
//        String url = "https://www.monkeyrocket.review/save_snap.php";
//        String USER_AGENT = "Mozilla/5.0";
//        String payload = "snap=" + s;
//        URL obj = new URL(url);
//        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
//        conn.setUseCaches(false);
//        conn.setRequestMethod("POST");
//        conn.setRequestProperty("Host", "www.monkeyrocket.review");
//        conn.setRequestProperty("User-Agent", USER_AGENT);
//        conn.setRequestProperty("Accept",
//                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//        conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
//        conn.setRequestProperty("Content-Type", "application/octet-stream");
//        conn.setRequestProperty("Content-Length", Integer.toString(payload.length()));
//
//        conn.setDoOutput(true);
//        conn.setDoInput(true);
//
//        // Send post request
//        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//        wr.writeBytes(payload);
//        wr.flush();
//        wr.close();
//        int responseCode = conn.getResponseCode();
//        Log.i("server response: ",  Integer.toString(responseCode));
//
//    }
/////////////////////////////

    public static String getMD5EncryptedString(byte[] encTarget){
        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(encTarget, 0, encTarget.length);
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while ( md5.length() < 32 ) {
            md5 = "0"+md5;
        }
        return md5;
    }

    public static void log_to_server_zip(File zip_archive, String endpoint) throws IOException {
        String url = "https://www.monkeyrocket.review/save_snap_"+endpoint+".php";
        String USER_AGENT = "Mozilla/5.0";
        //String payload = "snap=" + s;
        URL obj = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "www.monkeyrocket.review");
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("Content-Length", Integer.toString((int) zip_archive.length()));

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        FileInputStream fis = new FileInputStream(zip_archive);

        StringBuffer fileContent = new StringBuffer();

        byte[] buffer = new byte[(int) zip_archive.length()];
        int n;
        while ((n = fis.read(buffer)) != -1)
        {
            //fileContent.append(new String(buffer, 0, n));
            wr.write(buffer);
        }

        String md5check =  getMD5EncryptedString(buffer);

/*
        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            wr.writeBytes(String.valueOf(bytes));
            //zipOut.write(bytes, 0, length);
        }
*/

        //wr.writeBytes(payload);
        wr.flush();
        wr.close();
        int responseCode = conn.getResponseCode();
        Log.i("server response: ",  Integer.toString(responseCode));

        InputStream inputStream = conn.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));

        String result="";
        String line="";
        while((line=bufferedReader.readLine())!=null){
            result+=line;
        }

        if (result.equals(md5check)){
            Log.i("zip_data", zip_archive.getName()+" -- Success --");
            zip_archive.delete();
        } else {
            Log.i("zip_data", zip_archive.getName()+" -- FAILED --");

        }

/*
        Log.i("zip_data_back_pre", md5check);

        final int chunkSize = 2048;
        for (int i = 0; i < result.length(); i += chunkSize) {
//            Log.d("Usage Stats", s.substring(i, Math.min(s.length(), i + chunkSize)));
            Log.i("zip_data_back", result.substring(i, Math.min(result.length(), i + chunkSize)));
        }
*/

    }

    public Integer getBatteryPercentage()
    {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryLvl = level / (float) scale;
        Integer batteryPct = (int) (batteryLvl * 100);
        //Log.d("Battery Level: ",batteryPct);
        //Toast.makeText(this, "Battery Level: "+batteryPct, Toast.LENGTH_SHORT).show();
        return batteryPct;
    }

    public JSONObject installedApps() throws JSONException {
        JSONArray appListJson = new JSONArray();
        JSONObject appsJsonObj = new JSONObject();
        //String appList="";
        int count=0;
        List<PackageInfo> packList = getPackageManager().getInstalledPackages(0);
        for (int i=0; i < packList.size(); i++)
        {
            PackageInfo packInfo = packList.get(i);
            if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                //String appName = packInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                String appName = packInfo.applicationInfo.packageName;
                appListJson.put(appName);
                //appList += " " + appName + " \n";
                count++;
            }
        }
        //appList = "\n App Count: "+count+"\n"+appList;
        appsJsonObj.put("count", count);
        appsJsonObj.put("app_list", appListJson);
        return appsJsonObj;
    }



    public String getTopPackageName()
    {
        String topPackageName = null;

        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), this.getPackageName());
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;

        if (!granted){
            return "Error: GET_USAGE_STATS permission not granted";
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            topPackageName = foregroundTaskInfo.topActivity.getPackageName();
        }else{
            UsageStatsManager usage = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*1000, time);
            if (stats != null) {
                SortedMap<Long, UsageStats> runningTask = new TreeMap<Long,UsageStats>();
                for (UsageStats usageStats : stats) {
                    runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (runningTask.isEmpty()) {
                    topPackageName="None";
                }else {
                    topPackageName = runningTask.get(runningTask.lastKey()).getPackageName();
                }
            }
        }
        Log.e("Task List", "Current App in foreground is: " + topPackageName);
        return topPackageName;
    }

    private String isScreenOn(){

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return String.valueOf(pm.isInteractive());
        } else {
            return String.valueOf(pm.isScreenOn());
        }
    }

    private String isInSavedMode(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        boolean psv= pm.isPowerSaveMode();
        String spsv = String.valueOf(psv);
        return spsv;
    }

  /*  private String isIdle(){
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        return String.valueOf(pm.isDeviceIdleMode());
    }
*/

    private JSONArray showUsageStats() throws JSONException {

        JSONArray appStatsList = new JSONArray();

        UsageStatsManager usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String stats_data="";
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        stats_data = stats_data + "Start time: " + convertTime(start) +"\n"+
                "End time: " + convertTime(end) +"\n";

        Map<String, UsageStats> usageStats = usageStatsManager.queryAndAggregateUsageStats(start, end);

        for (String key: usageStats.keySet()){
            JSONObject appStats = new JSONObject();
            appStats.put("package_name", usageStats.get(key).getPackageName());
            appStats.put("last_time_used", convertTime(usageStats.get(key).getLastTimeUsed()));
            appStats.put("contents", usageStats.get(key).describeContents());
            appStats.put("first_timestamp", usageStats.get(key).getFirstTimeStamp());
            appStats.put("last_timestamp", usageStats.get(key).getLastTimeStamp());
            appStats.put("foreground_time", usageStats.get(key).getTotalTimeInForeground());

            /*stats_data = stats_data + "Package Name : " + usageStats.get(key).getPackageName() + "\n" +
                                      "Last Time Used: " +  convertTime(usageStats.get(key).getLastTimeUsed()) + "\n" +
                                      "Describe Contents: " +  usageStats.get(key).describeContents() + "\n" +
                                      "First Time Stamp : " +  convertTime(usageStats.get(key).getFirstTimeStamp()) + "\n" +
                                      "Last Time Stamp : " +  convertTime(usageStats.get(key).getLastTimeStamp()) + "\n" +
                                      "Total Time in Foreground : " +  usageStats.get(key).getTotalTimeInForeground() + " miliseconds" +"\n"+
                                       "============================================================================" + "\n";*/
            appStatsList.put(appStats);
        }

        return appStatsList;

    }

    public static String convertTime(Long timestamp_i){
        Date date = new Date(timestamp_i);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH);
        return format.format(date);
    }

    private String convertTime2(Long timestamp_i){
        Date date = new Date(timestamp_i);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
        return format.format(date);
    }

    private JSONObject getStoppedApps(Context context) throws JSONException {

        JSONArray appListJson = new JSONArray();
        JSONObject appsJsonObj = new JSONObject();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int count=0;

        for (ApplicationInfo packageInfo : packages) {

            if (isSTOPPED(packageInfo) && !isSYSTEM(packageInfo)) {
                appListJson.put(packageInfo.packageName);
                count++;
            }
        }
        appsJsonObj.put("count", count);
        appsJsonObj.put("app_list", appListJson);
        return appsJsonObj;
    }

    private static boolean isSTOPPED(ApplicationInfo pkgInfo) {

        return ((pkgInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0);
        //return ((pkgInfo.flags & ApplicationInfo.FLAG_SUSPENDED) == 0);
    }


    private JSONObject getSuspendedApps(Context context) throws JSONException {

        JSONArray appListJson = new JSONArray();
        JSONObject appsJsonObj = new JSONObject();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        int count=0;

        for (ApplicationInfo packageInfo : packages) {

            if (isSUSPENDED(packageInfo) && !isSYSTEM(packageInfo)) {
                appListJson.put(packageInfo.packageName);
                count++;
            }
        }
        appsJsonObj.put("count", count);
        appsJsonObj.put("app_list", appListJson);
        return appsJsonObj;
    }

    private static boolean isSUSPENDED(ApplicationInfo pkgInfo) {

        return ((pkgInfo.flags & ApplicationInfo.FLAG_SUSPENDED) != 0);
        //return ((pkgInfo.flags & ApplicationInfo.FLAG_SUSPENDED) == 0);
    }


    private static boolean isSYSTEM(ApplicationInfo pkgInfo) {

        return ((pkgInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public JSONObject getDevInfo(Context mContext) throws JSONException {

        //JSONArray jsonArr = new JSONArray();
        JSONObject devObj = new JSONObject();
        TelephonyManager tm = (TelephonyManager) getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        //devObj.put("phone_type",  tm.getPhoneType());

        if (tm.getPhoneType()==1){
            boolean t=tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT;
            devObj.put("sim_absent", String.valueOf(t));
        }

        /*devObj.put("serial",  android.os.Build.SERIAL);
        devObj.put("model", Build.MODEL);
        devObj.put("brand", Build.BRAND);
        devObj.put("manufacturer", Build.MANUFACTURER);
        devObj.put("design", Build.DEVICE);
        devObj.put("product", Build.PRODUCT);*/

        //String androidId = Settings.Secure.getString(getContentResolver(),
          //      Settings.Secure.ANDROID_ID);

        //devObj.put("androidID", androidId);

        return devObj;
    }

    public JSONObject getRam() throws JSONException {

        JSONObject devObj = new JSONObject();


        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 0x100000L;
        long totalMegs = mi.totalMem/ 0x100000L;

        devObj.put("available",  availableMegs);
        devObj.put("total",  totalMegs);

        return devObj;
    }


}



