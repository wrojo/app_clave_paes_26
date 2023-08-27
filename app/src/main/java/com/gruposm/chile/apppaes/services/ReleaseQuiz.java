package com.gruposm.chile.apppaes.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.gruposm.chile.apppaes.BuildConfig;
import com.gruposm.chile.apppaes.LoginActivity;
import com.gruposm.chile.apppaes.R;
import com.gruposm.chile.apppaes.object.Course;
import com.gruposm.chile.apppaes.object.Quiz;
import com.gruposm.chile.apppaes.object.Student;
import com.gruposm.chile.apppaes.utils.CallBackCourse;
import com.gruposm.chile.apppaes.utils.CallBackLogin;
import com.gruposm.chile.apppaes.utils.CallBackRelease;
import com.gruposm.chile.apppaes.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseQuiz {
    Context context;
    CallBackRelease callBack;
    private SessionManager session;
    ProgressDialog progress;
    public  ReleaseQuiz(Context context, Map<String, String> data, CallBackRelease callBack)
    {
        this.context = context;
        this.callBack = callBack;
        session =  new SessionManager(context);
        this.getData(data,callBack);
        progress = ProgressDialog.show(this.context, this.context.getResources().getString(R.string.title_dialog_proccess),
                this.context.getResources().getString(R.string.text_dialog_release_quiz), false);
    }
    private void getData(Map<String, String> data,final CallBackRelease callBack)
    {

        JSONObject jsonObject = new JSONObject();
        Log.d("TAG", "course_: " +  data.get("fecha_inicio"));
        try {
            jsonObject.put("fecha_inicio", data.get("fecha_inicio"));
            jsonObject.put("fecha_termino", data.get("fecha_termino"));
            jsonObject.put("notificar", data.get("notificar"));
        } catch (JSONException e) {
            callBack.onErrorServer();
        }
        String token = session.getUserDetail().get("TOKEN");
        String idUsuario = session.getUserDetail().get("ID");
        String courseId = data.get("course_id");
        String quizId = data.get("quiz_id");

        String urlWithParams = "https://www.desarrollosm.cl/desarrollo/local_waldo_paes/paes/release/" + quizId + "/" + courseId + ".json";
        if(BuildConfig.BUILD_TYPE != "debug")
        {
            urlWithParams = "https://clavepaes.cl/v4/paes/release/" + quizId + "/" + courseId + ".json";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,urlWithParams, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progress.dismiss();
                try {
                    int success =  response.getInt("success");
                    if(success == 1)
                    {
                        callBack.onSucess(true);
                    }
                    else
                    {
                        callBack.onSucess(false);
                    }

                } catch (JSONException e)
                {
                    callBack.onErrorServer();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                if(error.networkResponse == null)
                {
                    callBack.onErrorServer();
                    return;
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
            public Map<String, String> getHeaders() throws AuthFailureError {
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
