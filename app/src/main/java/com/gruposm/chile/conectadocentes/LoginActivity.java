package com.gruposm.chile.conectadocentes;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gruposm.chile.conectadocentes.database.ManageSql;
import com.gruposm.chile.conectadocentes.object.User;
import com.gruposm.chile.conectadocentes.services.Login;
import com.gruposm.chile.conectadocentes.services.SyncroDB;
import com.gruposm.chile.conectadocentes.utils.CallBackLogin;
import com.gruposm.chile.conectadocentes.utils.CallBackSyncro;
import com.gruposm.chile.conectadocentes.utils.SessionManager;
import com.gruposm.chile.conectadocentes.utils.SessionSyncro;
import com.gruposm.chile.conectadocentes.utils.Util;

public class LoginActivity extends AppCompatActivity {

    private View parent_view;
    private EditText etRut, etDv, etPassword;
    private Button btnLogin;
    ProgressDialog progress;
    private SessionManager session;
    private SessionSyncro sessionSyncro;
    private ManageSql manageSql;
    private static final String TAG = "LoginActivity";
    public static String[] storge_permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storge_permissions_33 = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
    };
    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = storge_permissions_33;
        } else {
            p = storge_permissions;
        }
        return p;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        parent_view = findViewById(R.id.lyt_parent);
        etRut = (EditText) findViewById(R.id.etRut);
        etDv = (EditText) findViewById(R.id.etDv);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        manageSql =  new ManageSql(this);
        session =  new SessionManager(this);
        sessionSyncro =  new SessionSyncro(this);

        LoginActivity.this.requestPermissions(permissions(),1);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasPermissions(LoginActivity.this,permissions()))
                {
                    progress = ProgressDialog.show(LoginActivity.this, getResources().getString(R.string.login_progress_title),
                            getResources().getString(R.string.login_progress_message), false);
                    String mRut = etRut.getText().toString().trim();
                    String mDv = etDv.getText().toString().trim();
                    String mPass = etPassword.getText().toString().trim();
                    if (!mRut.isEmpty() && !mDv.isEmpty() && !mPass.isEmpty()) {
                        Login login =  new Login(LoginActivity.this,mRut,mDv,mPass, new CallBackLogin() {

                            @Override
                            public void onSucess(User user) {
                                progress.dismiss();
                                session.createSession(user.getNombre(),user.getEmail(),user.getIdUsuario(),user.getAvatar(),user.getRol(),user.getToken(),user.getRefreshToken(),user.getUnique());
                                SyncroDB syncroDB =  new SyncroDB(LoginActivity.this, new CallBackSyncro() {
                                    @Override
                                    public void onSuccess(boolean success) {
                                        if(!success)
                                        {
                                            Util.toastIconError(LoginActivity.this,getResources().getString(R.string.login_progress_message_sincro_error));
                                        }
                                        else
                                        {
                                            Util.toastIconSuccess(LoginActivity.this,getResources().getString(R.string.login_successlogin_message));
                                            Intent i;
                                            i = new Intent(LoginActivity.this, CoursesActivity.class);
                                            startActivity(i);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onErrorServer() {
                                progress.dismiss();
                                Util.toastIconError(LoginActivity.this,getResources().getString(R.string.login_errorrespuesta_message));
                            }
                            @Override
                            public void onErrorUnauthorized()
                            {
                                progress.dismiss();
                                Util.toastIconError(LoginActivity.this,getResources().getString(R.string.login_errorlogin_message));
                            }
                            @Override
                            public void onErrorToken() {
                                progress.dismiss();
                                Util.toastIconError(LoginActivity.this,getResources().getString(R.string.text_error_token));
                            }
                        });
                    } else {
                        progress.dismiss();
                        etRut.setError(getResources().getString(R.string.login_error_rut));
                        etPassword.setError(getResources().getString(R.string.login_error_clave));
                    }

                }
                else
                {
                    showSettingsDialog();
                }
            }

        });
        if(session.isLoggin()){
            Intent i;
            i = new Intent(LoginActivity.this, CoursesActivity.class);
            startActivity(i);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

        // below line is the title for our alert dialog.
        builder.setTitle(R.string.permisosDenegados);

        // below line is our message for our dialog
        builder.setMessage(R.string.permisosCancelados);
        builder.setPositiveButton(R.string.permisosBtn, (dialog, which) -> {
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel();
            // below is the intent from which we are redirecting our user.
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 101);
        });
        builder.setNegativeButton(R.string.text_btn_cancel, (dialog, which) -> {
            // this method is called when user click on negative button.
            dialog.cancel();
        });
        // below line is used to display our dialog
        builder.show();
    }
}
