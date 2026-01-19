package com.gruposm.chile.appclavepaes26;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.gruposm.chile.appclavepaes26.adapters.AdapterQuiz;
import com.gruposm.chile.appclavepaes26.database.ManageSql;
import com.gruposm.chile.appclavepaes26.fragments.DialogRealeaseQuiz;
import com.gruposm.chile.appclavepaes26.object.Course;
import com.gruposm.chile.appclavepaes26.object.Inbox;
import com.gruposm.chile.appclavepaes26.services.SyncroDB;
import com.gruposm.chile.appclavepaes26.utils.CallBackDialog;
import com.gruposm.chile.appclavepaes26.utils.CallBackSyncro;
import com.gruposm.chile.appclavepaes26.utils.Tools;
import com.gruposm.chile.appclavepaes26.utils.Util;
import com.gruposm.chile.appclavepaes26.widgets.LineItemDecoration;

import java.util.List;

public class QuizzesActivity extends AppCompatActivity {
    private View parent_view;
    private View noItem;
    private RecyclerView recyclerView;
    private AdapterQuiz mAdapter;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ManageSql manageSql;
    private String courseId;
    List<Inbox> items;
    Course course;
    private String dateInit;
    private String timeInit;
    private String dateEnd;
    private String timeEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);
        parent_view = findViewById(R.id.lyt_parent);
        noItem = findViewById(R.id.no_item);
        initToolbar();
        initComponent();
    }
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_quizzes));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.paes_color_5);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    //getResources().getString(R.string.title_activity_courses)
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
    }
    private void initComponent() {
        manageSql =  new ManageSql(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);
        Intent intent = getIntent();
        courseId = intent.getStringExtra("curso_id");
        course =  new Course();
        course.setId(courseId);
        course.setNombre(intent.getStringExtra("nombre"));
        course.setNivel(intent.getStringExtra("nivel"));
        noItem.setVisibility(View.INVISIBLE);
        items = manageSql.ListQuizzes(course);
        if(items.size() == 0)
        {
            noItem.setVisibility(View.VISIBLE);
        }
        mAdapter = new AdapterQuiz(this, items);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnClickListener(new AdapterQuiz.OnClickListener() {
            @Override
            public void onItemClick(View view, Inbox obj, int pos) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    Inbox inbox = mAdapter.getItem(pos);
                    Intent i= new Intent(QuizzesActivity.this, StudentsActivity.class);
                    i.putExtra("curso_id",courseId);
                    i.putExtra("quiz_id",inbox.idStr);
                    startActivity(i);
                }
            }
            @Override
            public void onItemLongClick(View view, Inbox obj, int pos) {
                enableActionMode(pos);
            }

            @Override
            public void onItemChipClick(View view, Inbox obj, int pos) {
                Inbox inbox = mAdapter.getItem(pos);
                DialogRealeaseQuiz dialogRealeaseQuiz =  new DialogRealeaseQuiz(QuizzesActivity.this, inbox.quiz, course, new CallBackDialog() {
                    @Override
                    public void onSuccess(boolean success) {
                        if(success)
                        {
                            items.clear();
                            items.addAll(manageSql.ListQuizzes(course));
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });

        actionModeCallback = new QuizzesActivity.ActionModeCallback();

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
            Tools.setSystemBarColor(QuizzesActivity.this, R.color.blue_grey_700);
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
            Tools.setSystemBarColor(QuizzesActivity.this, R.color.red_600);
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
            callSyncro();
        }
        return super.onOptionsItemSelected(item);
    }
    private void callSyncro()
    {
        SyncroDB syncroDB =  new SyncroDB(QuizzesActivity.this, new CallBackSyncro() {
            @Override
            public void onSuccess(boolean success) {

                if(!success)
                {
                    Util.toastIconError(QuizzesActivity.this,getResources().getString(R.string.login_progress_message_sincro_error));
                }
                else
                {
                    items.clear();
                    items.addAll(manageSql.ListQuizzes(course));
                    mAdapter.notifyDataSetChanged();
                    Util.toastIconSuccess(QuizzesActivity.this,getResources().getString(R.string.login_progress_message_sincro_success));
                }
            }
        });
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