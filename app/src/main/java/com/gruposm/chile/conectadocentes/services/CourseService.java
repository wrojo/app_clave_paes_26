package com.gruposm.chile.conectadocentes.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.gruposm.chile.conectadocentes.BuildConfig;
import com.gruposm.chile.conectadocentes.object.AnswerSheet;
import com.gruposm.chile.conectadocentes.object.Course;
import com.gruposm.chile.conectadocentes.object.Quiz;
import com.gruposm.chile.conectadocentes.object.Result;
import com.gruposm.chile.conectadocentes.object.Student;
import com.gruposm.chile.conectadocentes.utils.CallBackCourse;
import com.gruposm.chile.conectadocentes.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseService {
    private final static String URLPOST = BuildConfig.SERVER_COURSE_SERVICE;
    Context context;
    CallBackCourse callBack;
    private SessionManager session;

    public CourseService(Context context, CallBackCourse callBack) {
        this.context = context;
        this.callBack = callBack;
        session =  new SessionManager(context);
        this.getData(callBack, false);
    }
    private void getData(final CallBackCourse callBack, final boolean isRetry)
    {
        JSONObject jsonObject = new JSONObject();
        String token = session.getUserDetail().get("TOKEN");
        String idUsuario = session.getUserDetail().get("ID");
        Log.d("TOKEN", "CourseService using token: " + token);
        Log.d("TAG", "url: " + URLPOST);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,URLPOST, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int success =  response.getInt("success");
                    JSONObject jsonObject =  response.getJSONObject("content");
                    JSONArray jsonArray = jsonObject.getJSONArray("cursos");
                    if(success == 1){
                        List<Course> items = new ArrayList<>();
                        for(int i=0; i<jsonArray.length(); i++)
                        {
                            JSONObject object =  jsonArray.getJSONObject(i);
                            String id = object.getString("id").trim();
                            String nombre = object.getString("nombre").trim();
                            String letra = object.getString("letra").trim();
                            String nivel = object.getString("nivel").trim();
                            JSONArray jsonQuizzes =  object.getJSONArray("ensayos");
                            JSONArray jsonStudents =  object.getJSONArray("estudiantes");
                            Course course =  new Course();
                            course.setId(id);
                            course.setNombre(nombre);
                            course.setLetra(letra);
                            course.setNivel(nivel);
                            course.setIdUsuario(idUsuario);
                            List<Quiz> quizzes =  setQuizzes(course,jsonQuizzes);
                            List<Student> students =  setStudentsWithQuizzes(course, jsonStudents, jsonQuizzes);
                            course.setQuizzes(quizzes);
                            course.setStudents(students);
                            items.add(course);
                        }
                        callBack.onSucess(items);
                    }
                } catch (JSONException e)
                {
                    callBack.onErrorServer();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null)
                {
                    callBack.onErrorServer();
                    return;
                }
                if(error.networkResponse.statusCode == 401)
                {
                    if (isRetry) {
                        session.logout();
                        callBack.onErrorUnauthorized();
                        return;
                    }
                    TokenRefresher.refresh(context, new TokenRefresher.Callback() {
                        @Override
                        public void onSuccess(String token, String refreshToken) {
                            getData(callBack, true);
                        }

                        @Override
                        public void onFailure() {
                            session.logout();
                            callBack.onErrorUnauthorized();
                        }
                    });
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
    private List<Quiz> setQuizzes(Course course, JSONArray jsonQuizzes)
    {
        List<Quiz> quizzes = new ArrayList<>();
        for(int c=0; c<jsonQuizzes.length(); c++)
        {

            JSONObject quizObject = null;
            try {
                quizObject = jsonQuizzes.getJSONObject(c);
                int id = quizObject.getInt("id");
                String tipo = quizObject.getString("tipo").trim();
                String nombre = quizObject.getString("nombre").trim();
                int totalPreguntas = quizObject.getInt("total_preguntas");
                int totalOpciones = quizObject.getInt("total_opciones");
                JSONArray jsonCorrectas =  quizObject.getJSONArray("correctas");
                String correctas = AnswerSheet.ANSWER_JSON_TO_STRING(jsonCorrectas);
                String fechaInicio = quizObject.getString("fecha_inicio");
                String fechaFin = quizObject.getString("fecha_fin");
                boolean liberado = quizObject.getBoolean("liberado");
                int esLiberado = 0;
                if(liberado)
                {
                    esLiberado = 1;
                }
                if(fechaInicio.equals("") ||  fechaInicio.equals("0000-00-00 00:00:00"))
                {
                    fechaInicio = null;
                }
                if(fechaFin.equals("") ||  fechaFin.equals("0000-00-00 00:00:00"))
                {
                    fechaFin = null;
                }
                String urlHojaRespuesta = quizObject.getString("url_hoja_respuesta");
                String origen = quizObject.optString("origen", "").trim();
                JSONArray jsonResults =  quizObject.getJSONArray("respuestas");
                List<Student> quizStudents = new ArrayList<>();
                if (quizObject.has("estudiantes") && !quizObject.isNull("estudiantes")) {
                    JSONArray jsonQuizStudents = quizObject.getJSONArray("estudiantes");
                    quizStudents = setStudents(course, jsonQuizStudents);
                }
                Quiz quiz = new Quiz();
                quiz.setId(String.valueOf(id));
                quiz.setTipo(tipo);
                quiz.setCursoId(course.getId());
                quiz.setNombre(nombre);
                quiz.setTotalPreguntas(totalPreguntas);
                quiz.setTotalOpciones(totalOpciones);
                quiz.setCorrectas(correctas);
                quiz.setFechaInicio(fechaInicio);
                quiz.setFechaFin(fechaFin);
                quiz.setLiberado(esLiberado);
                quiz.setUrlHojaRespuestas(urlHojaRespuesta);
                quiz.setOrigen(origen);
                List<Result> results = this.setResults(course,quiz,jsonResults);
                quiz.setResults(results);
                quiz.setStudents(quizStudents);
                quizzes.add(quiz);
            } catch (JSONException e) {
                Log.d("quiz", "quiz:" + e.getMessage());
                e.printStackTrace();
            }
        }
        return quizzes;
    }
    private List<Student> setStudents(Course course, JSONArray jsonStudents)
    {
        List<Student> students = new ArrayList<>();
        for(int c=0; c<jsonStudents.length(); c++)
        {
            JSONObject studentObject = null;
            try {
                studentObject = jsonStudents.getJSONObject(c);
                int id = studentObject.getInt("id");
                String apellidos = studentObject.getString("apellidos").trim();
                String nombres = studentObject.getString("nombres");
                String rut = studentObject.getString("rut").trim();

                Student student = new Student();
                student.setId(String.valueOf(id));
                student.setCursoId(course.getId());
                student.setApellidos(apellidos);
                student.setNombres(nombres);
                student.setRut(rut);
                Log.d("TAG", "___rut:" + student.getRut());
                students.add(student);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return students;
    }

    private List<Student> setStudentsWithQuizzes(Course course, JSONArray jsonStudents, JSONArray jsonQuizzes) {
        List<Student> students = setStudents(course, jsonStudents);
        HashMap<String, Student> byRut = new HashMap<>();
        for (Student student : students) {
            if (student.getRut() != null) {
                byRut.put(student.getRut(), student);
            }
        }
        for (int c = 0; c < jsonQuizzes.length(); c++) {
            try {
                JSONObject quizObject = jsonQuizzes.getJSONObject(c);
                if (!quizObject.has("estudiantes") || quizObject.isNull("estudiantes")) {
                    continue;
                }
                JSONArray jsonQuizStudents = quizObject.getJSONArray("estudiantes");
                List<Student> quizStudents = setStudents(course, jsonQuizStudents);
                for (Student student : quizStudents) {
                    String rut = student.getRut();
                    if (rut == null || byRut.containsKey(rut)) {
                        continue;
                    }
                    byRut.put(rut, student);
                    students.add(student);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return students;
    }
    private List<Result> setResults(Course course, Quiz quiz, JSONArray jsonResults)
    {
        List<Result> results = new ArrayList<>();
        for(int c=0; c<jsonResults.length(); c++)
        {
            JSONObject resultObject = null;
            try {
                resultObject = jsonResults.getJSONObject(c);
                String rut = resultObject.getString("rut");
                String fecha = resultObject.getString("fecha");
                int buenas = resultObject.getInt("buenas");
                int malas = resultObject.getInt("malas");
                int omitidas = resultObject.getInt("omitidas");
                int porcentaje = resultObject.getInt("porcentaje");
                String imagen = resultObject.getString("captura");
                JSONArray respuestas = resultObject.getJSONArray("respuestas");
                Gson gson = new Gson();
                String jsonRespuestas = gson.toJson(this.getLetters(respuestas));
                Log.d("quiz", "jsonRespuestas:" + jsonRespuestas);
                Result result = new Result();
                result.setEnsayo_id(quiz.getId());
                result.setRut(rut);
                result.setBuenas(buenas);
                result.setMalas(malas);
                result.setOmitidas(omitidas);
                result.setPorcentaje(porcentaje);
                result.setImagen(imagen);
                result.setRespuesta(jsonRespuestas);
                result.setFecha(fecha);
                result.setCurso_id(course.getId());
                result.setPorcentajeStr(String.valueOf(result.getPorcentaje())+" %");
                results.add(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
    private String[] getLetters(JSONArray jsonResults)
    {
        String[] letters = new String[jsonResults.length()];
        for(int c=0; c<jsonResults.length(); c++)
        {
            String letter = "";
            try {
                letter = (String) jsonResults.get(c);
                letters[c] = letter.toUpperCase();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return letters;
    }
}
