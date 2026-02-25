package cl.gruposm.conectaevaluaciones.services;

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
import cl.gruposm.conectaevaluaciones.BuildConfig;
import cl.gruposm.conectaevaluaciones.R;
import cl.gruposm.conectaevaluaciones.utils.CallBackRelease;
import cl.gruposm.conectaevaluaciones.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReleaseQuiz {
    private final static String URLPOST = BuildConfig.SERVER_RELEASE_SERVICE;
    Context context;
    CallBackRelease callBack;
    private SessionManager session;
    ProgressDialog progress;
    public  ReleaseQuiz(Context context, Map<String, String> data, CallBackRelease callBack)
    {
        this.context = context;
        this.callBack = callBack;
        session =  new SessionManager(context);
        this.getData(data,callBack, false);
        progress = ProgressDialog.show(this.context, this.context.getResources().getString(R.string.title_dialog_proccess),
                this.context.getResources().getString(R.string.text_dialog_release_quiz), false);
    }
    private void getData(Map<String, String> data,final CallBackRelease callBack, final boolean isRetry)
    {

        JSONObject jsonObject = new JSONObject();
        Log.d("SERVICERELEASE", "course_: " +  data.get("fecha_inicio"));
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
        Log.d("TOKEN", "ReleaseQuiz using token: " + token);

        String urlWithParams = URLPOST + "/" + quizId + "/" + courseId + ".json";
        // if(BuildConfig.BUILD_TYPE != "debug")
        // {
        //     urlWithParams = "https://clavepaes.cl/v4/paes/release/" + quizId + "/" + courseId + ".json";
        // }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlWithParams, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("SERVICERELEASE", "response:" + response.toString());
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
                    Log.d("SERVICERELEASE", "error:" + e.getMessage());
                    callBack.onErrorServer();


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SERVICERELEASE", "error:" + error.toString());
                if(error.networkResponse == null)
                {
                    progress.dismiss();
                    callBack.onErrorServer();
                    return;
                }
                if(error.networkResponse.statusCode == 401)
                {
                    if (isRetry) {
                        progress.dismiss();
                        session.logout();
                        callBack.onErrorUnauthorized();
                        return;
                    }
                    TokenRefresher.refresh(context, new TokenRefresher.Callback() {
                        @Override
                        public void onSuccess(String token, String refreshToken) {
                            getData(data, callBack, true);
                        }

                        @Override
                        public void onFailure() {
                            progress.dismiss();
                            session.logout();
                            callBack.onErrorUnauthorized();
                        }
                    });
                }
                else{
                    progress.dismiss();
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
