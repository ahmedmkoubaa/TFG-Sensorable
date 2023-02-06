package com.commons;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LoginHelper {
    public static boolean isLogged(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(SensorableConstants.LOGIN_DONE, false);
    }

    public static String getUserCode(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SensorableConstants.USER_SESSION_CODE, null);
    }

    public static void saveLogin(final Context context, final String userCode) {
        SharedPreferences saved_values = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = saved_values.edit();
        editor.putBoolean(SensorableConstants.LOGIN_DONE, true);
        editor.putString(SensorableConstants.USER_SESSION_CODE, userCode);
        editor.commit();
    }

    // the condition to have a verified and good user code
    public static boolean validateUserCode(String code) {
        return code.trim().length() > 3;
    }
}
