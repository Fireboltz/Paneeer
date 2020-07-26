package org.amfoss.paneeer.editorCamera.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceUtil {

    public static void putLongValue(Context context, String prefName, String key, long value) {
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLongValue(Context context, String prefName, String key) {
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        try {
            return pref.getLong(key, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    public static void putStringValue(Context context, String prefName, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringValue(Context context, String prefName, String key) {
        try {
            SharedPreferences pref = context.getSharedPreferences(prefName, 0);
            return pref.getString(key, null);
        } catch (Exception e) {
            return null;
        }
    }
}
