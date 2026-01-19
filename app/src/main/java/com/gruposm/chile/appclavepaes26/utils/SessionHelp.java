package com.gruposm.chile.appclavepaes26.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionHelp {

    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "VISTAAYUDA";
    private static final String VISTA = "IS_VISTA";


    public SessionHelp(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createSession(){

        editor.putBoolean(VISTA, true);
        editor.apply();

    }

    public boolean isVista(){
        return sharedPreferences.getBoolean(VISTA, false);
    }



}