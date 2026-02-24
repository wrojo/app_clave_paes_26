package com.gruposm.chile.conectadocentes.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gruposm.chile.conectadocentes.R;
import com.gruposm.chile.conectadocentes.object.Course;
import com.gruposm.chile.conectadocentes.object.Inbox;
import com.gruposm.chile.conectadocentes.object.Quiz;
import com.gruposm.chile.conectadocentes.object.Result;
import com.gruposm.chile.conectadocentes.object.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;

public class ManageSql {
    AdminSqlLite mySqlLite;
    private static String NAME_DB = "sm_app_paes";
    private static String TABLE_RESULTS = "resultados";
    private static String TABLE_COURSES = "cursos";
    private static String TABLE_QUIZZES = "ensayos";
    private static String TABLE_STUDENTS = "estudiantes";
    private static String TABLE_QUIZ_STUDENTS = "ensayo_estudiantes";
    private static final String TAG = "MANAGE_SQL";
    private SQLiteDatabase miDb;
    private Context ctx;

    public ManageSql(Context c) {
        mySqlLite = new AdminSqlLite(c,ManageSql.NAME_DB, null, 3);
        miDb = mySqlLite.getWritableDatabase();
        this.ctx = c;
    }

    public boolean syncro(List<Course> courses){
        boolean error = false;
        miDb.beginTransaction();
        int respDelete1 = miDb.delete(TABLE_COURSES,null,null);
        int respDelete2 = miDb.delete(TABLE_QUIZZES,null,null);
        int respDelete3 = miDb.delete(TABLE_STUDENTS,null,null);
        int respDelete4 = miDb.delete(TABLE_QUIZ_STUDENTS,null,null);
        if(respDelete1 < 0 || respDelete2 < 0 || respDelete3 < 0 || respDelete4 < 0)
        {
            miDb.endTransaction();
            miDb.close();
            return false;
        }
        for (Course course : courses)
        {
            List<Quiz> quizzes = course.getQuizzes();
            List<Student> students = course.getStudents();
            boolean isCourse =  this.findCourse(course);
            long resp;
            if(isCourse)
            {
                resp = this.updateCourse(course);
            }
            else
            {
                resp = this.insertCourse(course);
            }
            if(resp == -1)
            {
                error = true;
                break;
            }
            for (Quiz quiz : quizzes)
            {
                //boolean isQuiz =  this.findQuiz(quiz);
                boolean isQuiz =  false;
                long respQuiz;
                if(isQuiz)
                {
                    respQuiz = this.updateQuiz(quiz);
                }
                else
                {
                    respQuiz = this.insertQuiz(quiz);
                }
                if(respQuiz == -1)
                {
                    error = true;
                    break;
                }
                List<Result> results  = quiz.getResults();
                for(Result result : results)
                {
                    boolean isResult =  this.findResult(result, true);
                    long respResult = 0;
                    if(!isResult)
                    {
                        respResult = this.insertResult(result, true);
                    }
                    Log.d("quiz", "error_syncro:" + error);
                    if(respResult == -1)
                    {
                        error = true;
                        break;
                    }
                }
                List<Student> quizStudents = quiz.getStudents();
                if (quizStudents != null) {
                    for (Student student : quizStudents) {
                        long respQuizStudent = this.insertQuizStudent(quiz, student);
                        if (respQuizStudent == -1) {
                            error = true;
                            break;
                        }
                    }
                }
                if (error) {
                    break;
                }
            }
            if(error)
            {
                break;
            }
            for (Student student : students)
            {
                //boolean isStudent =  this.findStudent(student);
                boolean isStudent =  false;
                long respStudent;
                if(isStudent)
                {
                    respStudent = this.updateStudent(student);
                }
                else
                {
                    respStudent = this.insertStudent(student);
                }
                if(respStudent == -1)
                {
                    error = true;
                    break;
                }
            }
            if(error)
            {
                break;
            }
        }
        if(error)
        {
            miDb.endTransaction();
            miDb.close();
            return false;
        }
        miDb.setTransactionSuccessful();
        miDb.endTransaction();
        miDb.close();
        return true;
    }
    // Resultados
    public boolean findResult(Result result, boolean bulk)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{result.getEnsayo_id(),result.getRut(), result.getCurso_id()};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_RESULTS+" where ensayo_id = ? and  rut = ? and  curso_id = ?", args);
                if (c.getCount() > 0){
                    return true;
                }
            }
            catch(Exception err){
                err.printStackTrace();
            }
        }
        return false;
    }
    public boolean findResult(Result result)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{result.getEnsayo_id(),result.getRut(), result.getCurso_id()};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_RESULTS+" where ensayo_id = ? and  rut = ? and  curso_id = ?", args);
                if (c.getCount() > 0){
                    miDb.close();
                    return true;
                }
            }
            catch(Exception err){
                miDb.close();
                err.printStackTrace();
            }
        }
        miDb.close();
        return false;
    }
    public Result getResult(Result result)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{result.getEnsayo_id(),result.getRut(), result.getCurso_id()};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_RESULTS+" where ensayo_id = ? and  rut = ? and  curso_id = ?", args);
                if (c.getCount() > 0){
                    if (c.moveToPosition(0)) {
                        Result _result = new Result();
                        _result.setEnsayo_id(c.getString(0));
                        _result.setRut(c.getString(1));
                        _result.setBuenas(c.getInt(2));
                        _result.setMalas(c.getInt(3));
                        _result.setOmitidas(c.getInt(4));
                        _result.setPorcentaje(c.getInt(5));
                        _result.setImagen(c.getString(6));
                        _result.setRespuesta(c.getString(7));
                        _result.setFecha(c.getString(8));
                        _result.setCurso_id(c.getString(9));
                        _result.setPorcentajeStr(String.valueOf(_result.getPorcentaje())+" %");
                        miDb.close();
                        return _result;
                    }
                }
            }
            catch(Exception err){
                miDb.close();
                err.printStackTrace();
            }
        }
        miDb.close();
        return result;
    }
    public long updateResult(Result result)
    {
        SQLiteDatabase miDb = mySqlLite.getWritableDatabase();
        ContentValues register =  new ContentValues();
        register.put("ensayo_id",result.getEnsayo_id());
        register.put("rut",result.getRut());
        register.put("buenas",result.getBuenas());
        register.put("malas",result.getMalas());
        register.put("omitidas",result.getOmitidas());
        register.put("porcentaje",result.getPorcentaje());
        register.put("imagen",result.getImagen());
        register.put("fecha",result.getFecha());
        register.put("respuestas",result.getRespuesta());
        register.put("curso_id",result.getCurso_id());
        if(miDb!=null){
            String[] args = new String[]{result.getEnsayo_id(),result.getRut(),result.getCurso_id()};
            long resp = miDb.update(ManageSql.TABLE_RESULTS,register,"ensayo_id = ? and  rut = ? and  curso_id = ?",args);
            if(resp != -1){
                miDb.close();
                return resp;
            }
        }
        miDb.close();
        return -1;
    }
    public long insertResult(Result result, boolean bulk) {
        ContentValues register =  new ContentValues();
        register.put("ensayo_id",result.getEnsayo_id());
        register.put("rut",result.getRut());
        register.put("buenas",result.getBuenas());
        register.put("malas",result.getMalas());
        register.put("omitidas",result.getOmitidas());
        register.put("porcentaje",result.getPorcentaje());
        register.put("imagen",result.getImagen());
        register.put("fecha",result.getFecha());
        register.put("respuestas",result.getRespuesta());
        register.put("curso_id",result.getCurso_id());
        if(miDb!=null){
            long id = miDb.insert(ManageSql.TABLE_RESULTS,null,register);
            if(id != -1){
                return id;
            }
        }
        return -1;
    }
    public long insertResult(Result result){

        SQLiteDatabase miDb = mySqlLite.getWritableDatabase();
        ContentValues register =  new ContentValues();
        register.put("ensayo_id",result.getEnsayo_id());
        register.put("rut",result.getRut());
        register.put("buenas",result.getBuenas());
        register.put("malas",result.getMalas());
        register.put("omitidas",result.getOmitidas());
        register.put("porcentaje",result.getPorcentaje());
        register.put("imagen",result.getImagen());
        register.put("fecha",result.getFecha());
        register.put("respuestas",result.getRespuesta());
        register.put("curso_id",result.getCurso_id());
        if(miDb!=null){
            long id = miDb.insert(ManageSql.TABLE_RESULTS,null,register);
            if(id != -1){
                miDb.close();
                return id;
            }
        }
        miDb.close();
        return -1;
    }
    // Cursos
    public ArrayList<Inbox> ListCourses()
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        ArrayList<Inbox> items = new ArrayList<Inbox>();
        if(miDb!=null){
            try {
                try {
                    Cursor c = miDb.rawQuery("select * from "+ ManageSql.TABLE_COURSES + " ORDER BY nivel, nombre, letra ", new String[]{});
                    while (c.moveToNext()) {
                        Course course = new Course();
                        course.setId(c.getString(0));
                        course.setNombre(c.getString(1));
                        course.setNivel(c.getString(2));
                        course.setLetra(c.getString(3));
                        course.setIdUsuario(c.getString(4));
                        Inbox inbox =  new Inbox();
                        inbox.idStr = course.getId();
                        inbox.from = course.getNombre();
                        inbox.email = course.getNivel();
                        inbox.letter = course.getLetra();
                        items.add(inbox);
                    }
                    miDb.close();
                    return items;
                }
                catch(Exception err){
                    miDb.close();
                    err.printStackTrace();
                }
            }
            catch(Exception err){
                miDb.close();
                err.printStackTrace();
            }
        }
        miDb.close();
        return items;
    }
    public long insertCourse(Course course)
    {
        ContentValues register =  new ContentValues();
        register.put("id",course.getId());
        register.put("nombre",course.getNombre());
        register.put("nivel",course.getNivel());
        register.put("letra",course.getLetra());
        register.put("usuario_id",course.getIdUsuario());
        if(miDb!=null){
            long resp = miDb.insert(ManageSql.TABLE_COURSES,null,register);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public long updateCourse(Course course)
    {
        ContentValues register =  new ContentValues();
        register.put("id",course.getId());
        register.put("nombre",course.getNombre());
        register.put("nivel",course.getNivel());
        register.put("letra",course.getLetra());
        register.put("usuario_id",course.getIdUsuario());
        if(miDb!=null){
            String[] args = new String[]{String.valueOf(course.getId())};
            long resp = miDb.update(ManageSql.TABLE_COURSES,register,"id = ?",args);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public boolean findCourse(Course course)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{String.valueOf(course.getId())};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_COURSES+" where id = ?", args);
                if (c.getCount() > 0){
                    return true;
                }
            }
            catch(Exception err){
                err.printStackTrace();
            }
        }
        return false;
    }
    // Ensayos
    public ArrayList<Inbox> ListQuizzes(Course course)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        Log.d("TAG", "course:" + course.getId());
        ArrayList<Inbox> items = new ArrayList<Inbox>();
        if(miDb!=null){
            try {
                try {
                    String[] args = new String[]{String.valueOf(course.getId())};
                    Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_QUIZZES+" where curso_id = ?", args);
                    List<Quiz> quizzes = new ArrayList<>();
                    while (c.moveToNext()) {
                        Quiz quiz = new Quiz();
                        quiz.setId(c.getString(0));
                        quiz.setTipo(c.getString(1));
                        quiz.setFechaInicio(c.getString(2));
                        quiz.setFechaFin(c.getString(3));
                        quiz.setLiberado(c.getInt(4));
                        quiz.setUrlHojaRespuestas(c.getString(5));
                        quiz.setCursoId(c.getString(6));
                        quiz.setNombre(c.getString(7));
                        quiz.setTotalPreguntas(c.getInt(8));
                        quiz.setTotalOpciones(c.getInt(9));
                        quiz.setCorrectas(c.getString(10));
                        quiz.setOrigen(c.getString(11));
                        quizzes.add(quiz);
                    }
                    Map<String, List<Quiz>> grouped = new HashMap<>();
                    Map<String, String> originLabels = new HashMap<>();
                    for (Quiz quiz : quizzes) {
                        String originKey = resolveOriginKey(quiz);
                        Log.d(TAG, "quiz_origin id=" + quiz.getId()
                                + " nombre=" + quiz.getNombre()
                                + " origen=" + quiz.getOrigen()
                                + " tipo=" + quiz.getTipo()
                                + " url=" + quiz.getUrlHojaRespuestas()
                                + " key=" + originKey);
                        List<Quiz> list = grouped.get(originKey);
                        if (list == null) {
                            list = new ArrayList<>();
                            grouped.put(originKey, list);
                        }
                        list.add(quiz);
                        if (!originLabels.containsKey(originKey)) {
                            originLabels.put(originKey, resolveOriginLabel(quiz));
                        }
                    }
                    List<String> userKeys = new ArrayList<>();
                    List<String> otherKeys = new ArrayList<>();
                    for (String key : grouped.keySet()) {
                        if (isUserOriginKey(key)) {
                            userKeys.add(key);
                            continue;
                        }
                        if ("sin_origen".equals(key)) {
                            continue;
                        }
                        otherKeys.add(key);
                    }
                    Collections.sort(userKeys);
                    Collections.sort(otherKeys);
                    Log.d(TAG, "quiz_origin userKeys=" + userKeys + " otherKeys=" + otherKeys);
                    for (String key : userKeys) {
                        addOriginGroup(items, grouped, originLabels, key);
                    }
                    for (String key : otherKeys) {
                        addOriginGroup(items, grouped, originLabels, key);
                    }
                    addOriginGroup(items, grouped, originLabels, "sin_origen");
                    miDb.close();
                    return items;
                }
                catch(Exception err){
                    miDb.close();
                    err.printStackTrace();
                }
            }
            catch(Exception err){
                miDb.close();
                err.printStackTrace();
            }
        }
        miDb.close();
        return items;
    }
    public long insertQuiz(Quiz quiz)
    {
        ContentValues register =  new ContentValues();
        register.put("id",quiz.getId());
        register.put("tipo",quiz.getTipo());
        register.put("fecha_inicio",quiz.getFechaInicio());
        register.put("fecha_termino",quiz.getFechaFin());
        register.put("liberado",quiz.getLiberado());
        register.put("url_hoja_respuestas",quiz.getUrlHojaRespuestas());
        register.put("curso_id",quiz.getCursoId());
        register.put("nombre",quiz.getNombre());
        register.put("total_preguntas",quiz.getTotalPreguntas());
        register.put("total_opciones",quiz.getTotalOpciones());
        register.put("correctas",quiz.getCorrectas());
        register.put("origen",quiz.getOrigen());
        if(miDb!=null){
            long resp = miDb.insert(ManageSql.TABLE_QUIZZES,null,register);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public long updateQuiz(Quiz quiz)
    {
        ContentValues register =  new ContentValues();
        register.put("id",quiz.getId());
        register.put("tipo",quiz.getTipo());
        register.put("fecha_inicio",quiz.getFechaInicio());
        register.put("fecha_termino",quiz.getFechaFin());
        register.put("liberado",quiz.getLiberado());
        register.put("url_hoja_respuestas",quiz.getUrlHojaRespuestas());
        register.put("curso_id",quiz.getCursoId());
        register.put("nombre",quiz.getNombre());
        register.put("total_preguntas",quiz.getTotalPreguntas());
        register.put("total_opciones",quiz.getTotalOpciones());
        register.put("correctas",quiz.getCorrectas());
        register.put("origen",quiz.getOrigen());
        if(miDb!=null){
            String[] args = new String[]{String.valueOf(quiz.getId())};
            long resp = miDb.update(ManageSql.TABLE_QUIZZES,register,"id = ?",args);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public boolean findQuiz(Quiz quiz)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{String.valueOf(quiz.getId())};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_QUIZZES+" where id = ?", args);
                if (c.getCount() > 0){
                    return true;
                }
            }
            catch(Exception err){
                err.printStackTrace();
            }
        }
        return false;
    }
    public Quiz getQuiz(Quiz quiz, Course course)
    {

        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){
            try {
                String[] args = new String[]{String.valueOf(quiz.getId()), course.getId()};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_QUIZZES+" where id = ? and curso_id = ?", args);
                if (c.getCount() > 0){
                    if (c.moveToPosition(0)) {
                        Quiz _quiz = new Quiz();
                        _quiz.setId(c.getString(0));
                        _quiz.setTipo(c.getString(1));
                        _quiz.setFechaInicio(c.getString(2));
                        _quiz.setFechaFin(c.getString(3));
                        _quiz.setLiberado(c.getInt(4));
                        _quiz.setUrlHojaRespuestas(c.getString(5));
                        _quiz.setCursoId(c.getString(6));
                        _quiz.setNombre(c.getString(7));
                        _quiz.setTotalPreguntas(c.getInt(8));
                        _quiz.setTotalOpciones(c.getInt(9));
                        _quiz.setCorrectas(c.getString(10));
                        _quiz.setOrigen(c.getString(11));
                        miDb.close();
                        return _quiz;
                    }
                }
            }
            catch(Exception err){
                err.printStackTrace();
                miDb.close();
                return null;
            }
        }
        miDb.close();
        return null;
    }
    public long releaseQuiz(Quiz quiz, Course course)
    {
        ContentValues register =  new ContentValues();
        register.put("liberado",1);
        if(miDb!=null){
            String[] args = new String[]{String.valueOf(quiz.getId()), course.getId()};
            long resp = miDb.update(ManageSql.TABLE_QUIZZES,register,"id = ? and curso_id = ?",args);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }

    private String normalizeOriginKey(String origin) {
        if (origin == null) {
            return "sin_origen";
        }
        String cleaned = origin.trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            return "sin_origen";
        }
        return cleaned;
    }

    private boolean isUserOriginKey(String key) {
        if (key == null) {
            return false;
        }
        String cleaned = key.trim().toLowerCase(Locale.ROOT);
        if (cleaned.contains("usuario")) {
            return true;
        }
        if (cleaned.contains("mis evaluaciones")) {
            return true;
        }
        return cleaned.startsWith("mis ");
    }

    private String resolveOriginKey(Quiz quiz) {
        String origin = quiz.getOrigen();
        String normalized = normalizeOriginKey(origin);
        if (!"sin_origen".equals(normalized)) {
            return normalized;
        }
        String tipo = quiz.getTipo();
        String url = quiz.getUrlHojaRespuestas();
        String tipoKey = tipo != null ? tipo.toLowerCase(Locale.ROOT) : "";
        String urlKey = url != null ? url.toLowerCase(Locale.ROOT) : "";
        if (tipoKey.contains("sm") || urlKey.startsWith("sm_asset")) {
            return "sm";
        }
        return "usuario";
    }

    private String resolveOriginLabel(Quiz quiz) {
        String origin = quiz.getOrigen();
        if (origin != null && !origin.trim().isEmpty()) {
            return origin.trim();
        }
        String key = resolveOriginKey(quiz);
        if ("sm".equals(key)) {
            return "SM";
        }
        if ("usuario".equals(key)) {
            return "usuario";
        }
        return "Sin origen";
    }

    private String formatOriginTitle(String origin) {
        if (origin == null) {
            return "Sin origen";
        }
        String cleaned = origin.trim();
        if (cleaned.isEmpty()) {
            return "Sin origen";
        }
        return cleaned;
    }

    private void addOriginGroup(List<Inbox> items, Map<String, List<Quiz>> grouped, Map<String, String> originLabels, String key) {
        List<Quiz> list = grouped.get(key);
        if (list == null || list.isEmpty()) {
            return;
        }
        Collections.sort(list, new Comparator<Quiz>() {
            @Override
            public int compare(Quiz a, Quiz b) {
                String nameA = a.getNombre() != null ? a.getNombre() : "";
                String nameB = b.getNombre() != null ? b.getNombre() : "";
                return nameA.compareTo(nameB);
            }
        });
        Inbox header = new Inbox();
        header.isHeader = true;
        header.from = originLabels.get(key);
        items.add(header);
        for (Quiz quiz : list) {
            Inbox inbox = new Inbox();
            inbox.idStr = quiz.getId();
            inbox.from = quiz.getNombre();
            inbox.email = String.valueOf(quiz.getTotalPreguntas()) + " " + ctx.getResources().getString(R.string.text_answers);
            inbox.quiz = quiz;
            items.add(inbox);
        }
    }
    // Estudiantes
    public ArrayList<Inbox> ListStudents(Course course, Quiz quiz)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        ArrayList<Inbox> items = new ArrayList<Inbox>();
        if(miDb!=null){
            try {
                try {
                    String subSql = "";
                    subSql = "(select * from "+ TABLE_RESULTS +" where ensayo_id = ? and curso_id = ?)";
                    //subSql = TABLE_RESULTS;
                    String sql;
                    sql = "";
                    sql+=" select estudiante.id, estudiante.curso_id, estudiante.apellidos, estudiante.nombres, estudiante.rut, resultado.*";
                    sql+=" FROM " + TABLE_QUIZ_STUDENTS+ " estudiante LEFT JOIN " + subSql +" resultado ON estudiante.rut = resultado.rut";
                    sql+=" where estudiante.curso_id = ? and estudiante.ensayo_id = ?";
                    sql+=" GROUP BY estudiante.rut";
                    sql+=" ORDER BY estudiante.apellidos, estudiante.nombres";
                    String[] args = new String[]{quiz.getId(),course.getId(),course.getId(),quiz.getId()};
                    Cursor c = miDb.rawQuery(sql, args);
                    while (c.moveToNext()) {
                        Student student = new Student();
                        student.setId(c.getString(0));
                        student.setCursoId(c.getString(1));
                        student.setApellidos(c.getString(2));
                        student.setNombres(c.getString(3));
                        student.setRut(c.getString(4));
                        Result result =  new Result();
                        result.setEnsayo_id(c.getString(5));
                        result.setRut(student.getRut());
                        result.setEstudianteId(student.getId());
                        result.setBuenas(c.getInt(7));
                        result.setMalas(c.getInt(8));
                        result.setOmitidas(c.getInt(9));
                        result.setPorcentaje(c.getInt(10));
                        result.setPorcentajeStr(String.valueOf(result.getPorcentaje())+" %");
                        result.setImagen(c.getString(11));
                        result.setRespuesta(c.getString(12));
                        result.setFecha(c.getString(13));
                        String rutJoin =  c.getString(6);
                        boolean isHaveResult = false;
                        if(rutJoin != null)
                        {
                            isHaveResult = true;
                        }
                        Inbox inbox =  new Inbox();
                        //Log.d(TAG, "5join:" + c.getString(5));
                        String imgName = "alumno_bc8";
                        Resources res = ctx.getResources();
                        int idImage = res.getIdentifier(imgName, "drawable", ctx.getPackageName());
                        inbox.idStr = student.getId();
                        inbox.image = idImage;
                        inbox.from = student.getNombreCompleto();
                        inbox.buenas = result.getBuenas();
                        inbox.malas = result.getMalas();
                        inbox.omitidas = result.getOmitidas();
                        inbox.porcentaje = result.getPorcentajeStr();
                        inbox.isHaveResult =  isHaveResult;
                        inbox.result =  result;
                        inbox.student = student;
                        Log.d("TAG", "_rut:" + student.getRut());
                        Log.d("TAG", "__rut:" + rutJoin);
                        items.add(inbox);
                    }
                    return items;
                }
                catch(Exception err){
                    miDb.close();
                    err.printStackTrace();
                }
            }
            catch(Exception err){
                miDb.close();
                err.printStackTrace();
            }
        }
        miDb.close();
        return items;
    }

    public long insertQuizStudent(Quiz quiz, Student student) {
        ContentValues register = new ContentValues();
        register.put("id", student.getId());
        register.put("curso_id", student.getCursoId());
        register.put("apellidos", student.getApellidos());
        register.put("nombres", student.getNombres());
        register.put("rut", student.getRut());
        register.put("ensayo_id", quiz.getId());
        if (miDb != null) {
            long resp = miDb.insert(ManageSql.TABLE_QUIZ_STUDENTS, null, register);
            if (resp != -1) {
                return resp;
            }
        }
        return -1;
    }
    public long insertStudent(Student student)
    {
        ContentValues register =  new ContentValues();
        register.put("id",student.getId());
        register.put("curso_id",student.getCursoId());
        register.put("apellidos",student.getApellidos());
        register.put("nombres",student.getNombres());
        register.put("rut",student.getRut());
        if(miDb!=null){
            long resp = miDb.insert(ManageSql.TABLE_STUDENTS,null,register);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public long updateStudent(Student student)
    {
        ContentValues register =  new ContentValues();
        register.put("id",student.getId());
        register.put("curso_id",student.getCursoId());
        register.put("apellidos",student.getApellidos());
        register.put("nombres",student.getNombres());
        register.put("rut",student.getRut());
        if(miDb!=null){
            String[] args = new String[]{String.valueOf(student.getId())};
            long resp = miDb.update(ManageSql.TABLE_STUDENTS,register,"id = ?",args);
            if(resp != -1){
                return resp;
            }
        }
        return -1;
    }
    public boolean findStudent(Student student)
    {
        SQLiteDatabase miDb = mySqlLite.getReadableDatabase();
        if(miDb!=null){

            try {
                String[] args = new String[]{String.valueOf(student.getId())};
                Cursor c = miDb.rawQuery("select * from "+ManageSql.TABLE_STUDENTS+" where id = ?", args);
                if (c.getCount() > 0){
                    return true;
                }
            }
            catch(Exception err){
                err.printStackTrace();
            }
        }
        return false;
    }
}
