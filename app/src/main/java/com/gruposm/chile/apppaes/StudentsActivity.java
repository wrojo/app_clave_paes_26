package com.gruposm.chile.apppaes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gruposm.chile.apppaes.adapters.AdapterQuiz;
import com.gruposm.chile.apppaes.adapters.AdapterStudent;
import com.gruposm.chile.apppaes.database.ManageSql;
import com.gruposm.chile.apppaes.object.Course;
import com.gruposm.chile.apppaes.object.Inbox;
import com.gruposm.chile.apppaes.object.Quiz;
import com.gruposm.chile.apppaes.object.Result;
import com.gruposm.chile.apppaes.services.ScoreQuiz;
import com.gruposm.chile.apppaes.services.SyncroDB;
import com.gruposm.chile.apppaes.utils.CallBackScore;
import com.gruposm.chile.apppaes.utils.CallBackSyncro;
import com.gruposm.chile.apppaes.utils.SessionManager;
import com.gruposm.chile.apppaes.utils.Tools;
import com.gruposm.chile.apppaes.utils.Util;
import com.gruposm.chile.apppaes.widgets.LineItemDecoration;

import java.io.File;
import java.util.List;

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
                String url = quiz.getUrlHojaRespuestas();
                if(url == null || url.equals(""))
                {
                    return;
                }
                Intent link = new Intent(android.content.Intent.ACTION_VIEW);
                link.setData(Uri.parse(url));
                startActivity(link);
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