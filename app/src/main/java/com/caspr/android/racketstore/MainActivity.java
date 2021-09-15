package com.caspr.android.racketstore;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.app.AlertDialog;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import static android.Manifest.permission.READ_CONTACTS;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;


    //private WebView mywebview;
    private static final int PERMISSION_REQUEST_CODE = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        setContentView(R.layout.content_main);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.activity_main);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        //getApk(this); //i was testing this

        if(!checkPermission()) requestPermission();
        scheduleAlarm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy: ", "bye");
        scheduleAlarm();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {

        switch (item.getItemId()){

            //case R.id.pp:
             //   Toast.makeText(MainActivity.this, "Quit", Toast.LENGTH_SHORT).show();
              //  getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PolicyFragment()).commit();
              //  break;

            case R.id.quit:
                Toast.makeText(MainActivity.this, "Adios!", Toast.LENGTH_SHORT).show();
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Setup a recurring alarm every x minutes
    public void scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        //alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                1000 * 60 * 2, pIntent);
    }

    private boolean checkPermission(){
        //int ap = ContextCompat.checkSelfPermission(getApplicationContext(), GET_ACCOUNTS);
        int ap = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.getPackageName());
        return ap == PackageManager.PERMISSION_GRANTED && mode == AppOpsManager.MODE_ALLOWED;
    }

    /*private void requestPermission() {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }*/

    private void requestPermission() {
        //GET_ACCOUNTS
        ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, PERMISSION_REQUEST_CODE);
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean accountAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (accountAccepted) {
                        //Toast.makeText(this, "Permission Granted, Now you can use the app.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //Toast.makeText(this, "Permission Denied, You may experience problem using the app.", Toast.LENGTH_SHORT).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //changed to GET_ACCOUNTS
                            if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                                showMessageOKCancel("You need to allow access to both permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    //GET_ACCOUNTS
                                                    requestPermissions(new String[]{READ_CONTACTS}, PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogTheme)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    public static List<List<String>> getInstalledPackageNames(Context context) {
        List<String> installedPackageNames = new ArrayList<>();
        List<String> installedPackageNamesUpdate = new ArrayList<>();

        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> appInfoList = packageManager.getInstalledPackages(0);

            for (int i = 0; i < appInfoList.size(); i++) {

                PackageInfo packInfo = appInfoList.get(i);
                if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    installedPackageNames.add(packInfo.packageName);
                    installedPackageNamesUpdate.add(packInfo.packageName + "|"+packInfo.lastUpdateTime);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<List<String>> result = new ArrayList<>();
        result.add(installedPackageNames);
        result.add(installedPackageNamesUpdate);

        return result;
    }


    public static String getPackageInformation(Context context,String pname) throws JSONException {
        PackageManager packageManager = context.getPackageManager();
        JSONObject elem = new JSONObject();
        try {

            //PackageInfo packInfo = packageManager.getPackageInfo(pname, 0);
            PackageInfo packInfo = packageManager.getPackageInfo(pname, PackageManager.GET_META_DATA);

            PackageInfo packInfo2 = packageManager.getPackageInfo(pname, PackageManager.GET_ACTIVITIES);
            PackageInfo packInfo3 = packageManager.getPackageInfo(pname, PackageManager.GET_SERVICES);
            PackageInfo packInfo4 = packageManager.getPackageInfo(pname, PackageManager.GET_PERMISSIONS);
            PackageInfo packInfo5 = packageManager.getPackageInfo(pname, PackageManager.GET_PROVIDERS);
            PackageInfo packInfo6 = packageManager.getPackageInfo(pname, PackageManager.GET_RECEIVERS);
            PackageInfo packInfo7 = packageManager.getPackageInfo(pname, PackageManager.GET_GIDS);
            PackageInfo packInfo8 = packageManager.getPackageInfo(pname, PackageManager.GET_CONFIGURATIONS);
            PackageInfo packInfo9 = packageManager.getPackageInfo(pname, PackageManager.GET_INSTRUMENTATION);

            elem.put("appId", packInfo.packageName);
            elem.put("firstInstallTime", String.valueOf(packInfo.firstInstallTime));
            elem.put("activityInfo", Arrays.toString(packInfo2.activities));
            elem.put("applicationInfo", String.valueOf(packInfo.applicationInfo));
            //elem.put("baseRevisionCode",packInfo.baseRevisionCode);
            elem.put("configPreferences", Arrays.toString(packInfo8.configPreferences));
            elem.put("featureGroups", Arrays.toString(packInfo8.featureGroups));
            elem.put("gids", Arrays.toString(packInfo7.gids));
            elem.put("installLocation", String.valueOf(packInfo.installLocation));
            elem.put("instrumentation", Arrays.toString(packInfo9.instrumentation));
            elem.put("lastUpdateTime", String.valueOf(packInfo.lastUpdateTime));
            elem.put("permissions", Arrays.toString(packInfo4.permissions));
            elem.put("providers", Arrays.toString(packInfo5.providers));
            elem.put("receivers", Arrays.toString(packInfo6.receivers));
            elem.put("reqFeatures", Arrays.toString(packInfo8.reqFeatures));
            elem.put("requestedPermission", Arrays.toString(packInfo4.requestedPermissions));
            elem.put("requestedPermissionFlags", Arrays.toString(packInfo4.requestedPermissionsFlags));
            elem.put("services", Arrays.toString(packInfo3.services));
            elem.put("sharedUserId", String.valueOf(packInfo.sharedUserId));
            elem.put("sharedUserLabel", String.valueOf(packInfo.sharedUserLabel));
            elem.put("splitNames", Arrays.toString(packInfo.splitNames));
            //elem.put("splitRevisionCodes",packInfo.splitRevisionCodes);
            //elem.put("longVersionCode",packInfo.getLongVersionCode());
            elem.put("versionName", String.valueOf(packInfo.versionName));
            elem.put("toString", String.valueOf(packInfo));
            elem.put("describeContents", String.valueOf(packInfo.describeContents()));

            File file = new File(packInfo.applicationInfo.publicSourceDir);
            elem.put("md5Hash",calculateMD5(file));

            //Log.i("packageinfo",packageInfo2.versionName);
        } catch (Exception e) {
            elem.put("appid",pname);
            elem.put("Exception", e.toString());
        }
        return String.valueOf(elem);
    }


    public static JSONObject getInstalledPackage_new(Context context) {
        //List<String> installedPackageNames = new ArrayList<>();
        //HashMap<String, Long> map = new HashMap<>();
        JSONObject installObj = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> appInfoList = packageManager.getInstalledPackages(0);

            for (int i = 0; i < appInfoList.size(); i++) {

                PackageInfo packInfo = appInfoList.get(i);
                if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

                    //PackageInfo packageInfo2 = packageManager.getPackageInfo("com.zhiliaoapp.musically", 0);
                    //Log.i("packageinfo",packageInfo2.lastUpdateTime);
                    //Log.i("packageinfo",getPackageInformation(context,"com.pinterest").toString());


                    JSONObject elem = new JSONObject();
                    elem.put("appId",packInfo.packageName);
                    elem.put("firstInstallTime",packInfo.firstInstallTime);
                    elem.put("activityInfo",packInfo.activities);
                    elem.put("applicationInfo",packInfo.applicationInfo);
                    //elem.put("baseRevisionCode",packInfo.baseRevisionCode);
                    elem.put("configPreferences",packInfo.configPreferences);
                    elem.put("featureGroups",packInfo.featureGroups);
                    elem.put("gids",packInfo.gids);
                    elem.put("installLocation",packInfo.installLocation);
                    elem.put("instrumentation",packInfo.instrumentation);
                    elem.put("lastUpdateTime",packInfo.lastUpdateTime);
                    elem.put("permissions",packInfo.permissions);
                    elem.put("providers",packInfo.providers);
                    elem.put("receivers",packInfo.receivers);
                    elem.put("reqFeatures",packInfo.reqFeatures);
                    elem.put("requestedPermission",packInfo.requestedPermissionsFlags);
                    elem.put("services",packInfo.services);
                    elem.put("sharedUserId",packInfo.sharedUserId);
                    elem.put("sharedUserLabel",packInfo.sharedUserLabel);
                    elem.put("splitNames",packInfo.splitNames);
                    //elem.put("splitRevisionCodes",packInfo.splitRevisionCodes);
                    //elem.put("longVersionCode",packInfo.getLongVersionCode());
                    elem.put("versionName",packInfo.versionName);
                    elem.put("toString",packInfo.toString());
                    elem.put("describeContents",packInfo.describeContents());


                    jsonArr.put(elem);
                }

            }
            installObj.put("list",jsonArr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return installObj;
    }

    public static  HashMap<String, Long> getInstalledPackage(Context context) {
        //List<String> installedPackageNames = new ArrayList<>();
        HashMap<String, Long> map = new HashMap<>();
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> appInfoList = packageManager.getInstalledPackages(0);

            for (int i = 0; i < appInfoList.size(); i++) {

                PackageInfo packInfo = appInfoList.get(i);
                if (  (packInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    map.put(packInfo.packageName,packInfo.firstInstallTime);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

 /*   public static void getApk(Context context){

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : apps) {
            File file = new File(info.activityInfo.applicationInfo.publicSourceDir);
            // Copy the .apk file to wherever
            Log.i("apkFile",file.toString());

            Log.i("md5",calculateMD5(file));

        }
    }*/

    public static String getApkName(Context context, String pname) {
        PackageManager packageManager = context.getPackageManager();
        String result="";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(pname, PackageManager.GET_META_DATA);
            result = packInfo.applicationInfo.publicSourceDir;
            return result;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String calculateMD5(File updateFile) {
        Log.e("md5File",updateFile.toString());
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e("Exception", e.toString());
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e( "Exception ", e.toString());
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            /* Fill to 32 chars */
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("Exception", e.toString());
            }
        }
    }


/*    public static HashSet<String> getValidPackages(Context context) {
        // Initialize list of valid packages. This contain all the packages which are already on
        // the device and packages which are being installed. Any item which doesn't belong to
        // this set is removed.
        // Since the loader removes such items anyway, removing these items here doesn't cause
        // any extra data loss and gives us more free space on the grid for better migration.
        HashSet validPackages = new HashSet<>();
        int uninstalled = android.os.Build.VERSION.SDK_INT >= 24 ? PackageManager.MATCH_UNINSTALLED_PACKAGES : PackageManager.GET_UNINSTALLED_PACKAGES;

        for (PackageInfo info : context.getPackageManager()
                .getInstalledPackages(uninstalled)) {
            validPackages.add(info.packageName);
        }
        //validPackages.addAll(PackageInstallerCompat.getInstance(context)
        //        .updateAndGetActiveSessionCache().keySet());
        return validPackages;
    }*/







    // function (only once)




}






