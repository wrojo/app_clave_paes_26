package cl.gruposm.conectaevaluaciones;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.content.ContentResolver;
import android.content.ContentValues;
import java.io.OutputStream;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import cl.gruposm.conectaevaluaciones.adapters.AdapterStudent;
import cl.gruposm.conectaevaluaciones.database.ManageSql;
import cl.gruposm.conectaevaluaciones.object.Course;
import cl.gruposm.conectaevaluaciones.object.Inbox;
import cl.gruposm.conectaevaluaciones.object.Quiz;
import cl.gruposm.conectaevaluaciones.services.ScoreQuiz;
import cl.gruposm.conectaevaluaciones.services.SyncroDB;
import cl.gruposm.conectaevaluaciones.services.TokenRefresher;
import cl.gruposm.conectaevaluaciones.utils.CallBackScore;
import cl.gruposm.conectaevaluaciones.utils.CallBackSyncro;
import cl.gruposm.conectaevaluaciones.utils.SessionManager;
import cl.gruposm.conectaevaluaciones.utils.Tools;
import cl.gruposm.conectaevaluaciones.utils.Util;
import cl.gruposm.conectaevaluaciones.widgets.LineItemDecoration;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StudentsActivity extends AppCompatActivity {
    private View parent_view;
    private View noItem;
    private RecyclerView recyclerView;
    private AdapterStudent mAdapter;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ManageSql manageSql;
    private String courseId;
    private String quizId;
    private TextView textQuiz;
    private TextView textQuestions;
    private TextView textStudents;
    private Button btnDownload;
    private Button btnUploadResult;
    private SessionManager session;
    private ProgressDialog progressDownload;
    Course course;
    Quiz quiz;
    boolean errorScan;
    List<Inbox> items;
    FloatingActionButton floatingActionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students);
        noItem = findViewById(R.id.no_item);
        initToolbar();
        initComponent();
    }
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_students));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.paes_color_2);
    }
    private void initComponent() {
        parent_view = findViewById(R.id.lyt_parent);
        manageSql =  new ManageSql(this);
        session =  new SessionManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_escanear);
        textQuiz = (TextView) findViewById(R.id.textQuiz);
        textQuestions = (TextView) findViewById(R.id.textQuestions);
        textStudents = (TextView) findViewById(R.id.textStudents);
        btnDownload =  (Button) findViewById(R.id.btn_download);
        btnUploadResult =  (Button) findViewById(R.id.btnUploadResult);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);
        Intent intent = getIntent();
        courseId = intent.getStringExtra("curso_id");
        quizId = intent.getStringExtra("quiz_id");
        errorScan = intent.getBooleanExtra("error",false);
        course =  new Course();
        course.setId(courseId);
        quiz =  new Quiz();
        quiz.setId(quizId);
        noItem.setVisibility(View.INVISIBLE);
        items = manageSql.ListStudents(course,quiz);
        if(items.size() == 0)
        {
            noItem.setVisibility(View.VISIBLE);
        }
        quiz = this.manageSql.getQuiz(quiz, course);
        textQuiz.setText(quiz.getNombre());
        String textTotalStudents = getResources().getString(R.string.text_total_students, String.valueOf(items.size()));
        textQuestions.setText(quiz.getTotalPreguntas() +  " " + getResources().getString(R.string.text_answers));
        textStudents.setText(textTotalStudents);
        //set data and list adapter
        mAdapter = new AdapterStudent(this, items);
        recyclerView.setAdapter(mAdapter);
        floatingActionButton.bringToFront();
        floatingActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
                Intent i= new Intent(StudentsActivity.this, ScannerActivity.class);
                i.putExtra("curso_id",courseId);
                i.putExtra("quiz_id",quizId);
                startActivity(i);

            }
        });
        mAdapter.setOnClickListener(new AdapterStudent.OnClickListener() {
            @Override
            public void onItemClick(View view, Inbox obj, int pos) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    Inbox inbox = mAdapter.getItem(pos);
                    if(inbox.isHaveResult)
                    {
                        Intent i= new Intent(StudentsActivity.this, ResultActivity.class);
                        i.putExtra("correctas",quiz.getCorrectas());
                        i.putExtra("respuestas",inbox.result.getRespuesta());
                        i.putExtra("ensayo_id",quiz.getId());
                        i.putExtra("curso_id",course.getId());
                        i.putExtra("rut",inbox.student.getRut());
                        i.putExtra("estudiante_nombre",inbox.student.getNombreCompleto());
                        i.putExtra("estudiante_letra",inbox.student.getLetra());
                        i.putExtra("opciones",quiz.getTotalOpciones());
                        startActivity(i);
                        //Toast.makeText(getApplicationContext(), quiz.getCorrectas(), Toast.LENGTH_SHORT).show();
                        //showQuiz(inbox.result);
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.text_no_result_message) , Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, Inbox obj, int pos) {
                enableActionMode(pos);
            }
        });
        btnDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String ensayoId = null;
                if (items != null) {
                    for (Inbox inbox : items) {
                        if (inbox != null && inbox.isHaveResult && inbox.result != null) {
                            String candidate = inbox.result.getEnsayo_id();
                            if (candidate != null && !candidate.equals("")) {
                                ensayoId = candidate;
                                break;
                            }
                        }
                    }
                }
                if (ensayoId == null || ensayoId.equals("")) {
                    ensayoId = quiz.getId();
                }
                if (ensayoId == null || ensayoId.equals("")) {
                    return;
                }
                downloadSheet(ensayoId, false);
            }
        });
        btnUploadResult.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ScoreQuiz scoreQuiz =  new ScoreQuiz(StudentsActivity.this, items, new CallBackScore() {
                    @Override
                    public void onSucess(boolean success) {
                        if(success)
                        {
                            Util.toastIconSuccess(StudentsActivity.this,getResources().getString(R.string.text_success_upload_results));
                        }
                        else
                        {
                            Util.toastIconError(StudentsActivity.this,getResources().getString(R.string.text_error_upload_results));
                        }
                    }

                    @Override
                    public void onErrorServer() {
                        Util.toastIconError(StudentsActivity.this,getString(R.string.login_errorrespuesta_message));
                    }

                    @Override
                    public void onErrorUnauthorized() {
                        Util.toastIconError(StudentsActivity.this,getString(R.string.login_erroruser_message));
                        Util.logoutAndRedirect(StudentsActivity.this);
                    }

                    @Override
                    public void onErrorNoData() {
                        Util.toastIconError(StudentsActivity.this,getString(R.string.text_error_upload_results_no_items));
                    }
                });
            }
        });
        actionModeCallback = new StudentsActivity.ActionModeCallback();
        if(errorScan)
        {
            Util.showCustomDialogError(this, getResources().getString(R.string.title_error_dialog_scan), getResources().getString(R.string.text_error_data_anwsers_scan));
        }
    }

    private void downloadSheet(final String ensayoId, final boolean isRetry) {
        Log.d("SHEET", "downloadSheet ensayoId=" + ensayoId + " retry=" + isRetry);
        if (!isRetry) {
            progressDownload = ProgressDialog.show(StudentsActivity.this,
                    getResources().getString(R.string.title_dialog_proccess),
                    getResources().getString(R.string.text_dialog_download_sheet),
                    false);
        }
        String token = session.getUserDetail().get("TOKEN");
        if (token == null || token.equals("")) {
            if (progressDownload != null) {
                progressDownload.dismiss();
            }
            Util.logoutAndRedirect(StudentsActivity.this);
            return;
        }
        Uri uri = Uri.parse(BuildConfig.SERVER_SHEET_SERVICE).buildUpon()
                .appendQueryParameter("curso_evaluacion_id", ensayoId)
                .build();
        Log.d("SHEET", "url=" + uri.toString());
        Request<byte[]> request = new Request<byte[]>(Request.Method.GET, uri.toString(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SHEET", "error:" + error.toString());
                if (error.networkResponse != null) {
                    Log.d("SHEET", "statusCode:" + error.networkResponse.statusCode);
                    if (error.networkResponse.data != null) {
                        String body = new String(error.networkResponse.data);
                        Log.d("SHEET", "errorBody:" + body);
                    }
                }

                if (error.networkResponse == null) {
                    if (progressDownload != null) {
                        progressDownload.dismiss();
                    }
                    Util.toastIconError(StudentsActivity.this, getResources().getString(R.string.login_errorrespuesta_message));
                    return;
                }
                if (error.networkResponse.statusCode == 401) {
                    if (isRetry) {
                        if (progressDownload != null) {
                            progressDownload.dismiss();
                        }
                        session.logout();
                        Util.logoutAndRedirect(StudentsActivity.this);
                        return;
                    }
                    TokenRefresher.refresh(StudentsActivity.this, new TokenRefresher.Callback() {
                        @Override
                        public void onSuccess(String token, String refreshToken) {
                            downloadSheet(ensayoId, true);
                        }

                        @Override
                        public void onFailure() {
                            if (progressDownload != null) {
                                progressDownload.dismiss();
                            }
                            session.logout();
                            Util.logoutAndRedirect(StudentsActivity.this);
                        }
                    });
                    return;
                }
                if (progressDownload != null) {
                    progressDownload.dismiss();
                }
                Util.toastIconError(StudentsActivity.this, getResources().getString(R.string.login_errorrespuesta_message));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AuthorizationConecta", token);
                return headers;
            }

            @Override
            protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                if (response != null) {
                    String contentType = response.headers != null ? response.headers.get("Content-Type") : null;
                    String contentEncoding = response.headers != null ? response.headers.get("Content-Encoding") : null;
                    Log.d("SHEET", "statusCode:" + response.statusCode);
                    Log.d("SHEET", "contentType:" + contentType);
                    Log.d("SHEET", "contentEncoding:" + contentEncoding);
                    Log.d("SHEET", "bytes:" + (response.data != null ? response.data.length : 0));
                }
                return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected void deliverResponse(byte[] response) {
                if (response == null || response.length < 4 || response[0] != '%' || response[1] != 'P') {
                    String preview = "";
                    if (response != null) {
                        int len = Math.min(response.length, 200);
                        preview = new String(response, 0, len);
                    }
                    Log.d("SHEET", "invalidPdfPreview:" + preview);
                    if (progressDownload != null) {
                        progressDownload.dismiss();
                    }
                    Util.toastIconError(StudentsActivity.this, getResources().getString(R.string.login_errorrespuesta_message));
                    return;
                }
                String fileName = "hoja_respuesta_" + ensayoId + ".pdf";
                Uri savedUri = savePdfToDownloads(fileName, response);
                Log.d("SHEET", "savedUri=" + (savedUri != null ? savedUri.toString() : "null") + " fileName=" + fileName);
                if (progressDownload != null) {
                    progressDownload.dismiss();
                }
                if (savedUri != null) {
                    Toast.makeText(StudentsActivity.this, getResources().getString(R.string.text_success_download_sheet), Toast.LENGTH_SHORT).show();
                    openPdf(savedUri);
                } else {
                    Util.toastIconError(StudentsActivity.this, getResources().getString(R.string.login_errorrespuesta_message));
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    private Uri savePdfToDownloads(String fileName, byte[] data) {
        OutputStream out = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Log.d("SHEET", "savePdf: failed to insert MediaStore");
                    return null;
                }
                out = resolver.openOutputStream(uri);
                Log.d("SHEET", "savePdf: uri=" + uri.toString());
                if (out == null) {
                    Log.d("SHEET", "savePdf: output stream null");
                    return null;
                }
                out.write(data);
                out.flush();
                return uri;
            } else {
                java.io.File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists() && !dir.mkdirs()) {
                    Log.d("SHEET", "savePdf: failed to create downloads dir");
                    return null;
                }
                java.io.File file = new java.io.File(dir, fileName);
                out = new java.io.FileOutputStream(file);
                Log.d("SHEET", "savePdf: path=" + file.getAbsolutePath());
                out.write(data);
                out.flush();
                return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            }
        } catch (Exception e) {
            Log.d("SHEET", "savePdf error:" + e.getMessage());
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void openPdf(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Util.toastIconError(StudentsActivity.this, getResources().getString(R.string.login_errorrespuesta_message));
        }
    }
    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }
    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Tools.setSystemBarColor(StudentsActivity.this, R.color.blue_grey_700);
            mode.getMenuInflater().inflate(R.menu.menu_delete, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_delete) {
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelections();
            actionMode = null;
            Tools.setSystemBarColor(StudentsActivity.this, R.color.red_600);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_layout, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_syncro) {
            SyncroDB syncroDB =  new SyncroDB(StudentsActivity.this, new CallBackSyncro() {
                @Override
                public void onSuccess(boolean success) {

                    if(!success)
                    {
                        Util.snackBarIconError(StudentsActivity.this,parent_view,getResources().getString(R.string.login_progress_message_sincro_error));
                    }
                    else
                    {
                        items.clear();
                        items.addAll(manageSql.ListStudents(course,quiz));
                        mAdapter.notifyDataSetChanged();
                        Util.toastIconSuccess(StudentsActivity.this,getResources().getString(R.string.login_progress_message_sincro_success));
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    public boolean onNavigateUp(){
        return true;
    }
}
