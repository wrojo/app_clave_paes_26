package com.gruposm.chile.apppaes.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSqlLite extends SQLiteOpenHelper {
    public AdminSqlLite(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL("create table resultados (ensayo_id text, rut text, buenas integer, malas integer, omitidas integer, porcentaje integer, imagen text, respuestas text, fecha datetime, curso_id text)");
        sqLiteDatabase.execSQL("create table cursos (id text, nombre text, nivel text, letra text, usuario_id text)");
        sqLiteDatabase.execSQL("create table ensayos (id text, tipo text, fecha_inicio datetime, fecha_termino datetime, liberado integer default 0, url_hoja_respuestas text, curso_id text, nombre text, total_preguntas integer, total_opciones integer, correctas text)");
        sqLiteDatabase.execSQL("create table estudiantes (id text, curso_id text, apellidos text, nombres text, rut text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


    }
}
