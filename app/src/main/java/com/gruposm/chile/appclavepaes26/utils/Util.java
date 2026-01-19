package com.gruposm.chile.appclavepaes26.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import org.opencv.core.Point;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.android.material.snackbar.Snackbar;
import com.gruposm.chile.appclavepaes26.R;

public class Util {

    private static String FOLDER = "/SmPaesApp/";
    public static String SUFIX_IMAGE_NAME = "_screen.jpeg";
    public static String SEPARATOR_NAME_FILE = "_";

    public static String getPath(){


        try {


            String rootPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .getAbsolutePath() + Util.FOLDER;
            //String p = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
            //String  rootPath = p + "/SmApp/";
            File root = new File(rootPath);

            if (!root.exists()) {
                root.mkdirs();
            }

            return rootPath;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    public static String getNameQuizFile(String unique, String quizId, String rut){
        String fileName = unique + Util.SEPARATOR_NAME_FILE + quizId + Util.SEPARATOR_NAME_FILE + rut + Util.SUFIX_IMAGE_NAME;
        return fileName;
    }

    public static void eliminarArchivo(String path){


        try {


            File root = new File(path);

            if (root.exists()) {
                root.delete();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public static String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public static Bitmap decodeBase64(String input) {

        try{
            byte [] encodeByte=Base64.decode(input,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }



    }

    public static boolean saveImage(Bitmap bitmap, String path, String filename) {

        OutputStream outStream = null;
        File file = new File(path,filename);
        /*if (file.exists()) {
            file.delete();
            file = new File(path, filename);
        }*/
        try {
            // make a new bitmap from your file
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
            return true;
        } catch (Exception e) {
            Log.d("Util", "saveImage:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public static void SaveImageToSDCard(String Image, String path, String filename)
    {
        try{
            byte[] imageBytes=Base64.decode(Image, Base64.DEFAULT);
            InputStream is = new ByteArrayInputStream(imageBytes);
            Bitmap image=BitmapFactory.decodeStream(is);

            String mBaseFolderPath = path;


            String mFilePath = mBaseFolderPath + filename;

            File file = new File(mFilePath);

            FileOutputStream stream = new FileOutputStream(file);

            if (!file.exists()){
                file.createNewFile();
            }

            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            is.close();
            image.recycle();

            stream.flush();
            stream.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    public static String[] getFechaHora(String fecha){

        String[] fechaHora = new String[2];
        String[] splitFechaHora = fecha.split(" ");
        String[] splitFecha = splitFechaHora[0].split("-");
        String nuevaFecha = splitFecha[2]+"/"+splitFecha[1]+"/"+splitFecha[0];
        fechaHora[0] = nuevaFecha;
        fechaHora[1] = splitFechaHora[1];
        return fechaHora;
    }

    public static String getIcono(String eval){
        String prefijo = eval.substring(0,4);
        prefijo.toUpperCase();
        String nombre;
        if(prefijo.equals("SABI")){
            nombre = "biologia";
        }
        else if(prefijo.equals("SACN")){
            nombre = "naturales";
        }
        else if(prefijo.equals("SACS")){
            nombre = "sociales";
        }
        else if(prefijo.equals("SAFI")){
            nombre = "fisica";
        }
        else if(prefijo.equals("SALE")){
            nombre = "lenguaje";
        }
        else if(prefijo.equals("SAMA")){
            nombre = "matematica";
        }
        else if(prefijo.equals("SAQU")){
            nombre = "quimica";
        }

        else{

            nombre = "biologia";
        }
        return nombre;
    }
    public static boolean isRut(String rut) {

        boolean validate = false;
        try {
            rut =  rut.toUpperCase();
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

            char dv = rut.charAt(rut.length() - 1);

            int m = 0, s = 1;
            for (; rutAux != 0; rutAux /= 10) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
            }
            if (dv == (char) (s != 0 ? s + 47 : 75)) {
                validate = true;
            }

        } catch (java.lang.NumberFormatException e) {
        } catch (Exception e) {
        }
        return validate;
    }
    public static int pointsDist(Point p, Point q)
    {
        int dist = (int) ((p.x - q.x)*(p.x - q.x) + (p.y - q.y)*(p.y - q.y));
        return dist;
    }
    public static int diagonalSize(double side1, double side2)
    {
        int diagonal = (int) Math.sqrt(side1*side1+side2*side2);
        return diagonal;
    }
    public static int diagonalRectangle(Point a, Point b)
    {

        int cal1 = (int) ((b.x-a.x)*(b.x-a.x));
        int cal2 = (int) ((b.y-a.y)*(b.y-a.y));
        int diagonal = (int) Math.sqrt(cal1+cal2);
        return diagonal;
    }

    public static void toastIconSuccess(AppCompatActivity root, String texto) {
        Toast toast = new Toast(root.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = root.getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(texto);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_done);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(root.getResources().getColor(R.color.green_500));

        toast.setView(custom_view);
        toast.show();
    }

    public static void toastIconError(AppCompatActivity root, String texto) {
        Toast toast = new Toast(root.getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);

        //inflate view
        View custom_view = root.getLayoutInflater().inflate(R.layout.toast_icon_text, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(texto);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(root.getResources().getColor(R.color.red_600));

        toast.setView(custom_view);
        toast.show();
    }

    public static void snackBarIconError(AppCompatActivity root, View view, String texto) {
        final Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);
        //inflate view
        View custom_view = root.getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);

        ((TextView) custom_view.findViewById(R.id.message)).setText(texto);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_close);
        (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(root.getResources().getColor(R.color.red_600));
        snackBarView.addView(custom_view, 0);
        snackbar.show();
    }

    public static void snackBarIconSuccess(AppCompatActivity root, View view,String texto) {
        final Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT);

        //inflate view
        View custom_view = root.getLayoutInflater().inflate(R.layout.snackbar_icon_text, null);

        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackBarView = (Snackbar.SnackbarLayout) snackbar.getView();
        snackBarView.setPadding(0, 0, 0, 0);

        ((TextView) custom_view.findViewById(R.id.message)).setText(texto);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_done);
        (custom_view.findViewById(R.id.parent_view)).setBackgroundColor(root.getResources().getColor(R.color.green_500));
        snackBarView.addView(custom_view, 0);
        //snackbar.setDuration(500);
        snackbar.show();
    }

    public static void showDialogOK(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.btnAceptar, okListener)
                .setNegativeButton(R.string.btnCancelar, okListener)
                .create()
                .show();
    }

    public static void showCustomDialogError(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.title)).setText(title);
        ((TextView) dialog.findViewById(R.id.content)).setText(content);

        ((AppCompatButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }



}
