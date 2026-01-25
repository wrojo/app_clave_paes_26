package com.gruposm.chile.conectadocentes.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.gruposm.chile.conectadocentes.BuildConfig;
import com.gruposm.chile.conectadocentes.object.User;
import com.gruposm.chile.conectadocentes.utils.CallBackLogin;

import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.JsonObjectRequest;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Login {
    private final static String URLPOST = BuildConfig.SERVER_LOGIN_SERVICE;
    private String token;
    Context context;
    CallBackLogin callBack;
    public  Login(Context context, String rut, String dv, String pass, CallBackLogin callBack)
    {
        Map<String, String> data = new HashMap<String, String>();
        data.put("rut",rut);
        data.put("dv",dv);
        data.put("clave",pass);
        String rutPass = rut+":"+pass;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            token = Base64.getEncoder().encodeToString(rutPass.getBytes());
        }
        else
        {
            token = null;
        }

        this.context = context;
        this.callBack = callBack;
        this.getData( data,callBack);
    }
    private void getData(Map<String,String> data, final CallBackLogin callBack)
    {
        if(token == null)
        {
            callBack.onErrorToken();
            return;
        }
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            try {
                jsonObject.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                Log.d("TAG", "error:" + e.getMessage());
                callBack.onErrorServer();
                return;
            }
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,URLPOST, jsonObject, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.d("SERVICELOGIN", "response:" + response.toString());
                try
                {
                    int success =  response.getInt("success");
                    JSONObject jsonObject =  response.getJSONObject("content");
                    Log.d("TAG", "success:" + success);
                    if(success == 1)
                    {
                        token = jsonObject.getString("token");
                        String refreshToken = jsonObject.getString("refresh_token");
                        JSONObject jsonUserObject = jsonObject.getJSONObject("usuario");
                        String idUsuario =  String.valueOf(jsonUserObject.getInt("id"));
                        String rut = jsonUserObject.getString("rut").trim();
                        String avatar = jsonUserObject.getString("avatar");
                        String nombres = jsonUserObject.getString("nombres");
                        String apellidos = jsonUserObject.getString("apellidos");
                        String email = jsonUserObject.getString("email").trim();
                        String rol = jsonUserObject.getString("rol");
                        String unique = jsonUserObject.getString("unique_id");
                        User user =  new User();
                        user.setNombre(nombres + " " + apellidos);
                        user.setIdUsuario(idUsuario);
                        user.setAvatar(avatar);
                        user.setEmail(email);
                        user.setRol(rol);
                        user.setRut(rut);
                        user.setToken(token);
                        user.setRefreshToken(refreshToken);
                        user.setUnique(unique);
                        callBack.onSucess(user);
                    } else {
                        callBack.onErrorUnauthorized();
                    }

                } catch (JSONException e)
                {
                    Log.d("TAG", "error:" + e.getMessage());
                    callBack.onErrorServer();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                if(error.networkResponse == null)
                {
                    callBack.onErrorServer();
                    return;
                }
                if (error.networkResponse.data != null) {
                    String body = new String(error.networkResponse.data);
                    Log.d("TAG", "errorBody:" + body);
                }
                if(error.networkResponse.statusCode == 401)
                {
                    callBack.onErrorUnauthorized();
                }
                else{
                    callBack.onErrorServer();
                }
                Log.d("TAG", "statusCode:" + error.networkResponse.statusCode);
            }


        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError
            {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("AuthorizationConecta", token);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        requestQueue.add(jsonObjectRequest);
    }
}
