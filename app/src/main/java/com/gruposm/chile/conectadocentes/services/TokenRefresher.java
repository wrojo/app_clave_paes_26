package com.gruposm.chile.conectadocentes.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gruposm.chile.conectadocentes.BuildConfig;
import com.gruposm.chile.conectadocentes.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenRefresher {
    private static final String TAG = "TokenRefresher";

    public interface Callback {
        void onSuccess(String token, String refreshToken);
        void onFailure();
    }

    public static void refresh(Context context, Callback callback) {
        SessionManager session = new SessionManager(context);
        String refreshToken = session.getUserDetail().get(SessionManager.REFRESH_TOKEN);
        if (refreshToken == null || refreshToken.isEmpty()) {
            callback.onFailure();
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("refresh_token", refreshToken);
        } catch (JSONException e) {
            Log.d(TAG, "error:" + e.getMessage());
            callback.onFailure();
            return;
        }
        String url = BuildConfig.SERVER_UPDATE_TOKEN_SERVICE;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    if (success != 1) {
                        callback.onFailure();
                        return;
                    }
                    JSONObject content = response.getJSONObject("content");
                    String token = content.getString("token");
                    String newRefreshToken = content.getString("refresh_token");
                    Log.d("TOKEN", "refreshed token: " + token);
                    session.updateTokens(token, newRefreshToken);
                    callback.onSuccess(token, newRefreshToken);
                } catch (JSONException e) {
                    Log.d(TAG, "error:" + e.getMessage());
                    callback.onFailure();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailure();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }
}
