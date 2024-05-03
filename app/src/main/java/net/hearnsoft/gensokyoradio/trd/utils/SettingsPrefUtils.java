package net.hearnsoft.gensokyoradio.trd.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsPrefUtils {

    private static SettingsPrefUtils instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private SettingsPrefUtils(Context context) {
        sharedPreferences =
                context.getSharedPreferences(Constants.PREF_GLOBAL_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SettingsPrefUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsPrefUtils(context);
        }
        return instance;
    }

    public boolean readBooleanSettings(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public int readIntSettings(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public String readStringSettings(String key) {
        return sharedPreferences.getString(key, null);
    }

    public String readStringSettings(String key, String defValue) {
        return sharedPreferences.getString(key, defValue);
    }

    public boolean writeBooleanSettings(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean writeIntSettings(String key, int value) {
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean writeStringSettings(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

}
