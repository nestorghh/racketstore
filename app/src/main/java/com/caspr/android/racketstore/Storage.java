package com.caspr.android.racketstore;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;


import org.json.JSONException;
import org.json.JSONObject;

public class Storage{

    private String TAG = getClass().getName();

    String heading = "RAM Information";
    static long totalRamValue = totalRamMemorySize();
    static long freeRamValue = freeRamMemorySize();
    static long usedRamValue = totalRamValue - freeRamValue;

    static String totalInternalValue = getTotalInternalMemorySize();
    static String freeInternalValue = getAvailableInternalMemorySize();

    static String totalExternalValue = getTotalExternalMemorySize();
    static String freeExternalValue = getAvailableExternalMemorySize();

  /*  String internalMemoryTitle = "Internal Memory Information";
    static double totalInternalValue = getTotalInternalMemorySize()*0.000001;
    static double freeInternalValue = getAvailableInternalMemorySize()*0.000001;
    static double usedInternalValue = totalInternalValue - freeInternalValue;


    String externalMemoryTitle = "External Memory Information";
    static double totalExternalValue = getTotalExternalMemorySize()*0.000001;
    static double freeExternalValue = getAvailableExternalMemorySize()*0.000001;
    static double usedExternalValue = totalExternalValue - freeExternalValue*0.000001;*/


    private static long freeRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        long availableMegs = mi.availMem / 1048576L;

        return availableMegs;
    }

    private static long totalRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        long availableMegs = mi.totalMem / 1048576L;
        return availableMegs;
    }

    public static Boolean externalMemoryAvailable() {
        Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        //Boolean isSDSupportedDevice = Environment.isExternalStorageRemovable();

        //return isSDPresent && isSDSupportedDevice;
        return isSDPresent;
    }

    @SuppressWarnings("deprecation")
    public static String getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        Log.i( "InternalMemory: ",path.getAbsolutePath());
        Log.i( "TotalInternalMemory",String.valueOf(path.getTotalSpace()));
        Log.i( "UsaInternalMemory: ",String.valueOf(path.getUsableSpace()));
        Log.i( "FreeInternalMemory: ",String.valueOf(path.getFreeSpace()));
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocks();
        return formatSize(availableBlocks * blockSize);
    }

    @SuppressWarnings("deprecation")
    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize);
    }

    @SuppressWarnings("deprecation")
    public static String getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocks();
            return formatSize(availableBlocks * blockSize);
        } else {
            return "0";
        }
    }

    @SuppressWarnings("deprecation")
    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            Log.i( "ExternalMemory: ",path.getAbsolutePath());
            Log.i( "ExternalMemorySpace: ",String.valueOf(path.getTotalSpace()));
            Log.i( "UsaExternalMemory: ",String.valueOf(path.getUsableSpace()));
            Log.i( "FreeExternalMemory: ",String.valueOf(path.getFreeSpace()));
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatSize(totalBlocks * blockSize);
        } else {
            return "0";
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private static String returnToDecimalPlaces(long values){
        DecimalFormat df = new DecimalFormat("#.00");
        String angleFormated = df.format(values);
        return angleFormated;
    }

    public static JSONObject getDevStorage(Context mContext) throws JSONException {

        //JSONArray jsonArr = new JSONArray();
        JSONObject devObj = new JSONObject();
        devObj.put("sdcardAvailable", String.valueOf(externalMemoryAvailable()));
        devObj.put("total_external",  totalExternalValue);
        devObj.put("free_external", freeExternalValue);
        devObj.put("total_internal", totalInternalValue);
        devObj.put("free_internal", freeInternalValue);

        return devObj;
    }

}
