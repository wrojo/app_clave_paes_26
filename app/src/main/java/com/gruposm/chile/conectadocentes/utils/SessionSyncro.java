package com.gruposm.chile.conectadocentes.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SessionSyncro {

    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "SYNCRODB";
    private static final String LAST_DATE = "last_date";


    public SessionSyncro(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void setLastDate(){
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        editor.putString(LAST_DATE, dateFormat.format(date).toString());
        editor.apply();
    }

    public String getLastDate(){
        return sharedPreferences.getString(LAST_DATE, "");
    }



}