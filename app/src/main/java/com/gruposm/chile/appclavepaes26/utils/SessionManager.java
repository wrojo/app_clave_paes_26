package com.gruposm.chile.appclavepaes26.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "LOGIN";
    private static final String LOGIN = "IS_LOGIN";
    public static final String NAME = "NAME";
    public static final String EMAIL = "EMAIL";
    public static final String ID = "ID";
    public static final String AVATAR = "AVATAR";
    public static final String ROL = "ROL";
    public static final String TOKEN = "TOKEN";
    public static final String UNIQUE = "UNIQUE";

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createSession(String name, String email, String id, String avatar,String rol, String token , String unique){

        editor.putBoolean(LOGIN, true);
        editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.putString(ID, id);
        editor.putString(AVATAR, avatar);
        editor.putString(ROL, rol);
        editor.putString(TOKEN, token);
        editor.putString(UNIQUE, unique);
        editor.apply();

    }

    public boolean isLoggin(){
        return sharedPreferences.getBoolean(LOGIN, false);
    }

    public void checkLogin(){

        if (!this.isLoggin()){
          /*  Intent i = new Intent(context, LoginActivity.class);
            context.startActivity(i);
            ((HomeActivity) context).finish();*/
        }
    }

    public HashMap<String, String> getUserDetail(){

        HashMap<String, String> user = new HashMap<>();
        user.put(NAME, sharedPreferences.getString(NAME, null));
        user.put(EMAIL, sharedPreferences.getString(EMAIL, null));
        user.put(ID, sharedPreferences.getString(ID, null));
        user.put(AVATAR, sharedPreferences.getString(AVATAR, null));
        user.put(ROL, sharedPreferences.getString(ROL, null));
        user.put(TOKEN, sharedPreferences.getString(TOKEN, null));
        user.put(UNIQUE, sharedPreferences.getString(UNIQUE, null));

        return user;
    }


    public boolean isProfe(){

        try {
            String rol = sharedPreferences.getString(ROL, null);
            if (rol.equals("1")) {
                return true;
            }
        }catch (Exception ex){

            return true;
        }

        return false;

    }
    public void logout(){

        editor.clear();
        editor.commit();
    }

}