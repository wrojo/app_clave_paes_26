package cl.gruposm.conectaevaluaciones.services;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import cl.gruposm.conectaevaluaciones.BuildConfig;
import cl.gruposm.conectaevaluaciones.R;
import cl.gruposm.conectaevaluaciones.object.Inbox;
import cl.gruposm.conectaevaluaciones.object.Result;
import cl.gruposm.conectaevaluaciones.utils.CallBackScore;
import cl.gruposm.conectaevaluaciones.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreQuiz {
    Context context;
    CallBackScore callBack;
    private SessionManager session;
    ProgressDialog progress;
    List<Inbox> results;
    private final static String URL = BuildConfig.SERVER_BULK_RESULT_SERVICE;
    public  ScoreQuiz(Context context, List<Inbox> results, CallBackScore callBack)
    {
        this.context = context;
        this.callBack = callBack;
        this.results = results;
        session =  new SessionManager(context);
        progress = ProgressDialog.show(this.context, this.context.getResources().getString(R.string.title_dialog_proccess),
                this.context.getResources().getString(R.string.text_progress_upload_results), false);
        this.getData(results, callBack, false);
    }
    private void getData(List<Inbox> results,final CallBackScore callBack, final boolean isRetry)
    {

        String token = session.getUserDetail().get("TOKEN");
        String idUsuario = session.getUserDetail().get("ID");
        Log.d("TOKEN", "ScoreQuiz using token: " + token);
        JSONArray jsonArray =  new JSONArray();

        // Prepara data
        for (Inbox inbox:results)
        {
            if(!inbox.isHaveResult)
            {
                continue;
            }
            Result result =  inbox.result;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("paes_ensayo_id", result.getEnsayo_id());
                jsonObject.put("user_id", result.getEstudianteId());
                jsonObject.put("respuestas", result.getRespuesta());
                jsonObject.put("fecha", result.getFecha());
                jsonObject.put("captura", result.getImagen());
                jsonObject.put("buenas", result.getBuenas());
                jsonObject.put("malas", result.getMalas());
                jsonObject.put("omitidas", result.getOmitidas());
                jsonObject.put("porcentaje", result.getPorcentaje());
                Log.d("quiz", "data:" + result.getBuenas());
                Log.d("quiz", "data:" + result.getMalas());
                Log.d("quiz", "data:" + result.getBuenas());
                Log.d("quiz", "data:" + result.getOmitidas());
                Log.d("quiz", "data:" + result.getImagen());
            } catch (JSONException e) {
                callBack.onErrorServer();
            }
            jsonArray.put(jsonObject);
        }
        if(jsonArray.length() == 0)
        {
            progress.dismiss();
            callBack.onErrorNoData();
            return;
        }
        Log.d("quiz", "jsonArray:" + jsonArray.toString());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST,URL, jsonArray, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
              Log.d("quiz", "response:" + response);
                progress.dismiss();
                JSONObject respJson = null;
                try
                {
                    respJson = (JSONObject) response.get(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int success = 0;
                if(respJson != null)
                {
                    try {
                        success =  respJson.getInt("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(success == 1)
                {
                    callBack.onSucess(true);
                }
                else
                {
                    callBack.onSucess(false);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("quiz", "error:" + error.getLocalizedMessage());
                Log.d("quiz", "error:" + error.getMessage());
                if(error.networkResponse == null)
                {
                    progress.dismiss();
                    callBack.onErrorServer();
                    return;
                }
                if (error.networkResponse.data != null) {
                    String body = new String(error.networkResponse.data);
                    Log.d("quiz", "errorBody:" + body);
                }
                Log.d("quiz", "error:" + error.networkResponse.statusCode);
                Log.d("quiz", "error:" + error.toString());
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
                            getData(results, callBack, true);
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
            }

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("AuthorizationConecta", token);
                return headers;
            }
            //Important part to convert response to JSON Array Again
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String responseString;
                JSONArray array = new JSONArray();
                if (response != null) {

                    try {
                        responseString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                        JSONObject obj = new JSONObject(responseString);
                        (array).put(obj);
                    } catch (Exception ex) {
                    }
                }
                //return array;
                return Response.success(array, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this.context);
        requestQueue.add(jsonArrayRequest);
    }
}
