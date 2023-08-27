package com.gruposm.chile.apppaes;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.gruposm.chile.apppaes.adapters.AdapterListInbox;
import com.gruposm.chile.apppaes.database.ManageSql;
import com.gruposm.chile.apppaes.object.Inbox;
import com.gruposm.chile.apppaes.services.SyncroDB;
import com.gruposm.chile.apppaes.utils.CallBackSyncro;
import com.gruposm.chile.apppaes.utils.SessionHelp;
import com.gruposm.chile.apppaes.utils.SessionManager;
import com.gruposm.chile.apppaes.utils.Tools;
import com.gruposm.chile.apppaes.utils.Util;
import com.gruposm.chile.apppaes.widgets.LineItemDecoration;

import java.util.List;

public class CoursesActivity extends AppCompatActivity {
    private View parent_view;
    private View noItem;
    private RecyclerView recyclerView;
    private AdapterListInbox mAdapter;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private Toolbar toolbar;
    private ManageSql manageSql;
    private SessionManager session;
    private SessionHelp sessionHelp;
    List<Inbox> items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        parent_view = findViewById(R.id.lyt_parent);
        noItem = findViewById(R.id.no_item);
        initToolbar();
        initComponent();
        initNavigationMenu();
    }
    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_courses));
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
        session =  new SessionManager(this);
        sessionHelp =  new SessionHelp(this);
        if(!sessionHelp.isVista())
        {
            Intent i = new Intent(CoursesActivity.this, HelpActivity.class);
            startActivity(i);
        }
        noItem.setVisibility(View.INVISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LineItemDecoration(this, LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);
        items = manageSql.ListCourses();
        if(items.size() == 0)
        {
            noItem.setVisibility(View.VISIBLE);
        }
        //set data and list adapter
        mAdapter = new AdapterListInbox(this, items);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnClickListener(new AdapterListInbox.OnClickListener() {
            @Override
            public void onItemClick(View view, Inbox obj, int pos) {
                if (mAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos);
                } else {
                    // read the inbox which removes bold from the row
                    Inbox inbox = mAdapter.getItem(pos);
                    Intent i= new Intent(CoursesActivity.this, QuizzesActivity.class);
                    i.putExtra("curso_id",inbox.idStr);
                    i.putExtra("nombre",inbox.from);
                    i.putExtra("nivel",inbox.email+ " "+ inbox.letter);
                    startActivity(i);
                    //Toast.makeText(getApplicationContext(), "Read: " + inbox.from, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(View view, Inbox obj, int pos) {
                enableActionMode(pos);
            }
        });

        actionModeCallback = new ActionModeCallback();

    }
    private void initNavigationMenu() {
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        TextView textName = (TextView) findViewById(R.id.textName);
        TextView textEmail = (TextView) findViewById(R.id.textEmail);
        //LinearLayout btnMenu1 = (LinearLayout) findViewById(R.id.btnMenu1);
        LinearLayout btnMenu2 = (LinearLayout) findViewById(R.id.btnMenu2);
        LinearLayout btnMenu3 = (LinearLayout) findViewById(R.id.btnMenu3);
        LinearLayout btnMenu4 = (LinearLayout) findViewById(R.id.btnMenu4);
        LinearLayout btnMenu5 = (LinearLayout) findViewById(R.id.btnMenu5);
        btnMenu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://tutoriales.clavepaes.cl/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        btnMenu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(CoursesActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
        btnMenu4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
        btnMenu5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://creditos.clavepaes.cl/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
        textName.setText(session.getUserDetail().get("NAME"));
        textEmail.setText(session.getUserDetail().get("EMAIL"));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // open drawer at start
        //drawer.openDrawer(GravityCompat.END);
    }
    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.text_btn_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                session.logout();
                finish();
                Intent intent= new Intent(CoursesActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.text_btn_cancel, null);
        builder.show();
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
            SyncroDB syncroDB =  new SyncroDB(CoursesActivity.this, new CallBackSyncro() {
                @Override
                public void onSuccess(boolean success) {

                    if(!success)
                    {
                        Util.snackBarIconError(CoursesActivity.this,parent_view,getResources().getString(R.string.login_progress_message_sincro_error));
                    }
                    else
                    {
                        items.clear();
                        items.addAll(manageSql.ListCourses());
                        mAdapter.notifyDataSetChanged();
                        Util.toastIconSuccess(CoursesActivity.this,getResources().getString(R.string.login_progress_message_sincro_success));
                    }
                }
            });
        }
        return super.onOptionsItemSelected(item);
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
            Tools.setSystemBarColor(CoursesActivity.this, R.color.blue_grey_700);
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
            Tools.setSystemBarColor(CoursesActivity.this, R.color.red_600);
        }
    }
}