package cl.gruposm.conectaevaluaciones;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import cl.gruposm.conectaevaluaciones.adapters.AdapterResult;
import cl.gruposm.conectaevaluaciones.database.ManageSql;
import cl.gruposm.conectaevaluaciones.object.AnswerSheet;
import cl.gruposm.conectaevaluaciones.object.Result;
import cl.gruposm.conectaevaluaciones.object.RowResult;
import cl.gruposm.conectaevaluaciones.services.SyncroDB;
import cl.gruposm.conectaevaluaciones.utils.CallBackSyncro;
import cl.gruposm.conectaevaluaciones.utils.SessionManager;
import cl.gruposm.conectaevaluaciones.utils.Tools;
import cl.gruposm.conectaevaluaciones.utils.Util;
import cl.gruposm.conectaevaluaciones.widgets.LineItemDecoration;

import java.util.List;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {
    private View parent_view;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ManageSql manageSql;
    private AdapterResult mAdapter;
    private ResultActivity.ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private TextView txtBuenas,txtMalas,txtOmitidas,txtPorcentaje,txtName,letter;
    String estudianteNombreDialogo = "";
    List<RowResult> items;
    private SessionManager session;
    private Button btnShowImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        initToolbar();
        initComponent();
    }
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_result));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.paes_color_2);
    }
    private void initComponent() {
        parent_view = findViewById(R.id.lyt_parent);
        manageSql =  new ManageSql(this);
        session =  new SessionManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);
        txtBuenas = (TextView) findViewById(R.id.txtBuenas);
        txtMalas = (TextView) findViewById(R.id.txtMalas);
        txtOmitidas = (TextView) findViewById(R.id.txtOmitidas);
        txtPorcentaje = (TextView) findViewById(R.id.txtPorcentaje);
        txtName = (TextView) findViewById(R.id.txtName);
        letter = (TextView) findViewById(R.id.letter);
        btnShowImage = (Button) findViewById(R.id.btnShowImage);
        Intent intent = getIntent();
        String corrects = intent.getStringExtra("correctas");
        String answers = intent.getStringExtra("respuestas");
        String ensayoId = intent.getStringExtra("ensayo_id");
        String cursoId = intent.getStringExtra("curso_id");
        String estudianteNombre = intent.getStringExtra("estudiante_nombre");
        estudianteNombreDialogo = estudianteNombre;
        String estudianteLetra = intent.getStringExtra("estudiante_letra");
        String rut = intent.getStringExtra("rut");
        int options = intent.getIntExtra("opciones",5);
        Log.d("TAG", "_rut:" + rut);
        Result result = new Result();
        result.setCurso_id(cursoId);
        result.setEnsayo_id(ensayoId);
        result.setRut(rut);
        result = manageSql.getResult(result);
        txtBuenas.setText(String.valueOf(result.getBuenas()));
        txtMalas.setText(String.valueOf(result.getMalas()));
        txtOmitidas.setText(String.valueOf(result.getOmitidas()));
        txtPorcentaje.setText(result.getPorcentajeStr());
        txtName.setText(estudianteNombre);
        letter.setText(estudianteLetra);
        Map<Integer, String> mapCorrects = AnswerSheet.ANSWER_STRING_TO_MAP(corrects);
        items = AnswerSheet.ANSWERS_TO_ROW(mapCorrects,answers);
        mAdapter = new AdapterResult(this, items,options);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnClickListener(new AdapterResult.OnClickListener() {
            @Override
            public void onItemClick(View view, RowResult obj, int pos) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    RowResult row = mAdapter.getItem(pos);

                }
            }
        });
        Result finalResult = result;
        btnShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuiz(finalResult);
            }
        });
        actionModeCallback = new ResultActivity.ActionModeCallback();

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
            Tools.setSystemBarColor(ResultActivity.this, R.color.blue_grey_700);
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
            Tools.setSystemBarColor(ResultActivity.this, R.color.red_600);
        }
    }

    private void showQuiz(Result result) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_light);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
       // String nameImage = Util.getNameQuizFile(session.getUserDetail().get("UNIQUE"),result.getEnsayo_id(),result.getRut());
        //Log.d("Students", "nameImage:" + nameImage);
        //File imgFile = new  File(Util.getPath()+nameImage);
        String imageCode = result.getImagen();
        if(!imageCode.isEmpty()){
            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap myBitmap = Util.decodeBase64(imageCode);
            ImageView img =  ((ImageView) dialog.findViewById(R.id.image));
            img.setImageBitmap(myBitmap);
            ((TextView) dialog.findViewById(R.id.txtNombre)).setText(estudianteNombreDialogo);
            ((TextView) dialog.findViewById(R.id.txtAlumno)).setText(result.getRut());
            ((TextView) dialog.findViewById(R.id.txtPorcentaje)).setText(result.getPorcentajeStr());
        }
        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.menu_syncro) {
            SyncroDB syncroDB =  new SyncroDB(ResultActivity.this, new CallBackSyncro() {
                @Override
                public void onSuccess(boolean success) {

                    if(!success)
                    {
                        Util.snackBarIconError(ResultActivity.this,parent_view,getResources().getString(R.string.login_progress_message_sincro_error));
                    }
                    else
                    {
                       /* items.clear();
                        items.addAll(manageSql.ListStudents(course,quiz));
                        mAdapter.notifyDataSetChanged();
                        Util.toastIconSuccess(StudentsActivity.this,getResources().getString(R.string.login_progress_message_sincro_success));*/
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