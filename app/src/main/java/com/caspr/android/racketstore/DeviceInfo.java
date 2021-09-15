package com.caspr.android.racketstore;

import android.content.Context;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceInfo {

    public static JSONObject getDevInfo(Context mContext) throws JSONException {

        //JSONArray jsonArr = new JSONArray();
        JSONObject devObj = new JSONObject();

        devObj.put("serial",  android.os.Build.SERIAL);
        devObj.put("model", Build.MODEL);
        devObj.put("brand", Build.BRAND);
        devObj.put("manufacturer", Build.MANUFACTURER);
        devObj.put("design", Build.DEVICE);
        devObj.put("product", Build.PRODUCT);

        return devObj;
    }


}
