package cl.gruposm.conectaevaluaciones;

import static org.opencv.imgproc.Imgproc.rectangle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import cl.gruposm.conectaevaluaciones.database.ManageSql;
import cl.gruposm.conectaevaluaciones.detection.DetectionUtil;
import cl.gruposm.conectaevaluaciones.drawing.DrawOverScreen;
import cl.gruposm.conectaevaluaciones.object.Course;
import cl.gruposm.conectaevaluaciones.object.Quiz;
import cl.gruposm.conectaevaluaciones.object.Result;
import cl.gruposm.conectaevaluaciones.object.Student;
import cl.gruposm.conectaevaluaciones.utils.SessionManager;
import cl.gruposm.conectaevaluaciones.utils.Tools;
import cl.gruposm.conectaevaluaciones.utils.Util;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class ScannerActivity extends AppCompatActivity implements ImageAnalysis.Analyzer, View.OnClickListener {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static final String TAG = "ScannerActivity";
    PreviewView previewView;
    RelativeLayout layoutParent;
    private DrawOverScreen drawOverScreen;
    private DetectionUtil detectionUtil;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Mat rgb, gray, warped;
    boolean findRut = false;
    ImageView grayView;
    ImageView backGround;
    private Button btnAceptar;
    private Button btnCancelar;
    private LinearLayout layoutTexto;
    private LinearLayout layoutMensaje;
    private TextView txtResultado;
    private TextView txtMensaje;
    private Student student;
    private Toolbar toolbar;
    private ManageSql manageSql;
    private String quizId;
    private String courseId;
    private Bitmap currentBitmap;
    private SessionManager session;
    Map<String, String> results;
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        this.grayView = findViewById(R.id.grayView);
        this.backGround = findViewById(R.id.backGround);
        this.previewView = findViewById(R.id.previewView);
        this.layoutParent = findViewById(R.id.layoutParent);
        this.btnAceptar = (Button)this.findViewById(R.id.btnAceptar);
        this.btnCancelar = (Button)this.findViewById(R.id.btnCancelar);
        this.layoutTexto = (LinearLayout)this.findViewById(R.id.layoutTexto);
        this.layoutMensaje = (LinearLayout)this.findViewById(R.id.layoutMensaje);
        this.txtResultado = (TextView)this.findViewById(R.id.txtResultado);
        this.txtMensaje = (TextView)this.findViewById(R.id.txtMensaje);
        this.currentBitmap = null;
        session =  new SessionManager(this);
        this.manageSql =  new ManageSql(this);
        drawOverScreen =  new DrawOverScreen(this);
        layoutParent.addView(drawOverScreen);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        student =  new Student();
        // Obtener datos pasados como paramaetro
        Intent intent = getIntent();
        courseId = intent.getStringExtra("curso_id");
        quizId = intent.getStringExtra("quiz_id");
        // Iniiciar objecto de detección
        Course course =  new Course();
        course.setId(courseId);
        Quiz quiz =  new Quiz();
        quiz.setId(quizId);
        quiz = this.manageSql.getQuiz(quiz,course);
        this.detectionUtil =  new DetectionUtil(quiz);
        if(this.detectionUtil.isErrorDetection)
        {
            exitView(1);
            return;
        }
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
        this.hideButtons();
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               findRut = false;
               student.setRut(null);
               grayView.setImageBitmap(null);
               hideButtons();
            }
        });
        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageCode = "";
                if(currentBitmap != null)
                {

                    String fileName = Util.getNameQuizFile(session.getUserDetail().get("UNIQUE"), quizId, student.getRut());
                    imageCode = Util.getStringImage(currentBitmap);
                    Log.d(TAG, "fileName:" + fileName);
                    Log.d(TAG, "path:" + Util.getPath());
                    boolean isSaveImage = Util.saveImage(currentBitmap,Util.getPath(),fileName);
                }

                Result result =  new Result();
                result.setEnsayo_id(quizId);
                result.setRut(student.getRut());
                result.setBuenas(Integer.parseInt(results.get("corrects")));
                result.setMalas(Integer.parseInt(results.get("incorrects")));
                result.setOmitidas(Integer.parseInt(results.get("omitteds")));
                result.setPorcentaje(Integer.parseInt(results.get("percentages")));
                result.setImagen(imageCode);
                result.setFecha(Tools.getCurrentDateTimeMysql());
                result.setRespuesta(results.get("json"));
                result.setCurso_id(course.getId());
                boolean isResult = manageSql.findResult(result);
                long resp;
                if(isResult)
                {
                    resp = manageSql.updateResult(result);
                }
                else
                {
                    resp = manageSql.insertResult(result);
                }
                if(resp > 0)
                {
                    Util.toastIconSuccess(ScannerActivity.this,getResources().getString(R.string.oksave));
                }
                else
                {
                    Util.toastIconError(ScannerActivity.this,getResources().getString(R.string.errorsave));
                }
                findRut = false;
                student.setRut(null);
                grayView.setImageBitmap(null);
                hideButtons();
            }
        });
        initToolbar();
    }
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_scanner));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.paes_color_5);
    }

    Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        Preview preview = new Preview.Builder()
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        // Video capture use case
        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        // Image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(getExecutor(), this);

        //bind to lifecycle:
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        if(findRut)
        {
            this.hideMessage();
            image.close();
            return;
        }
        Bitmap bitmap = null;
        try
        {
            bitmap = previewView.getBitmap();
        }
        catch(Exception ex)
        {
            bitmap = null;
        }
        image.close();
        if(bitmap == null)
        {
            return;
        }
        if(bitmap.getWidth() == 0 || bitmap.getHeight() == 0)
        {
            return;
        }
        this.showMessage(getResources().getString(R.string.scannerTxtInfo));
        rgb = new Mat();
        gray =  new Mat();
        Utils.bitmapToMat(bitmap,rgb);
        Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_BGR2GRAY);
        Map<String, Integer> calculates = detectionUtil.calculateSquare(bitmap);
        drawOverScreen.setPosY(calculates.get("posY"));
        drawOverScreen.setRectWidth(calculates.get("w"));
        drawOverScreen.setWidth(calculates.get("cols"));
        drawOverScreen.setHeight(calculates.get("rows"));
        drawOverScreen.setInitY(calculates.get("initY"));
        drawOverScreen.invalidate();
        Map<Integer, Rect> rectanglesScreen = detectionUtil.markScreen(bitmap);
        boolean validFourPoint = detectionUtil.findFourPoint(rgb,rectanglesScreen,drawOverScreen);
        if(!validFourPoint)
        {
            return;
        }
        this.showMessage(getResources().getString(R.string.scannerTxtValidating));
        warped = detectionUtil.adjustPerpective(rgb);
        String rut = detectionUtil.findRut(warped);
        if(rut == null)
        {
            this.showMessage(getResources().getString(R.string.scannerTxtErrorRut));
            return;
        }
        student.setRut(rut);
        Log.d("TAG", "_rut:" + rut);
        Mat th = detectionUtil.findAnswers(warped);
        Mat finalPaper = detectionUtil.drawAnswer(warped);
        results = detectionUtil.printResult();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bm = Bitmap.createBitmap(finalPaper.cols(), finalPaper.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(finalPaper,bm);
                currentBitmap = bm;
                grayView.setImageBitmap(bm);
                String strCorrect = String.valueOf(results.get("corrects"));
                String strTotal = String.valueOf(results.get("total"));
                String strPercentage= String.valueOf(results.get("percentages"))+"%";
                String messageResult = getResources().getString(R.string.scannerTxtResult, String.valueOf(student.getRut()), strCorrect,strTotal,strPercentage);
                ScannerActivity.this.txtResultado.setText(messageResult);
                ScannerActivity.this.showButtons();
            }
        });
        findRut = true;
    }
    public void hideButtons(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnAceptar.setVisibility(View.GONE);
                btnCancelar.setVisibility(View.GONE);
                layoutTexto.setVisibility(View.GONE);
                layoutMensaje.setVisibility(View.GONE);
                backGround.setVisibility(View.GONE);
            }
        });
    }
    public void showButtons(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnAceptar.setVisibility(View.VISIBLE);
                btnCancelar.setVisibility(View.VISIBLE);
                layoutTexto.setVisibility(View.VISIBLE);
                backGround.setVisibility(View.VISIBLE);
            }
        });
    }
    public void showMessage(String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutMensaje.setVisibility(View.VISIBLE);
                txtMensaje.setText(message);
            }
        });
    }
    public void hideMessage(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                layoutMensaje.setVisibility(View.GONE);
                txtMensaje.setText("");
            }
        });
    }
    private void exitView(int error)
    {
        finish();
        Intent i = new Intent(ScannerActivity.this, StudentsActivity.class);
        i.putExtra("curso_id",courseId);
        i.putExtra("quiz_id",quizId);
        if(error ==  1)
        {
            i.putExtra("error",true);
        }
        startActivity(i);
        return;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            exitView(0);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed(){
        exitView(0);
    }

    @Override
    public boolean onNavigateUp(){
        exitView(0);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");



        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(ScannerActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(ScannerActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }
}