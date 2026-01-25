package com.gruposm.chile.conectadocentes.services;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.gruposm.chile.conectadocentes.R;
import com.gruposm.chile.conectadocentes.database.ManageSql;
import com.gruposm.chile.conectadocentes.object.Course;
import com.gruposm.chile.conectadocentes.utils.CallBackCourse;
import com.gruposm.chile.conectadocentes.utils.CallBackSyncro;
import com.gruposm.chile.conectadocentes.utils.Util;

import java.util.List;

public class SyncroDB {
    ProgressDialog progress;
    private ManageSql manageSql;

    public SyncroDB(Context ctx, CallBackSyncro callBackSyncro) {
        manageSql =  new ManageSql(ctx);
        progress = ProgressDialog.show(ctx, ctx.getResources().getString(R.string.login_progress_message_sincro),
                ctx.getResources().getString(R.string.login_progress_message_sincro_detail), false);
        CourseService courseService =  new CourseService(ctx, new CallBackCourse() {
            @Override
            public void onSucess(List<Course> courses) {
                progress.dismiss();
                boolean resultSyncro = manageSql.syncro(courses);
                if(!resultSyncro)
                {
                    callBackSyncro.onSuccess(false);
                }
                else
                {
                    callBackSyncro.onSuccess(true);
                }
            }
            @Override
            public void onErrorServer() {
                progress.dismiss();
                Util.toastIconError((AppCompatActivity) ctx,ctx.getResources().getString(R.string.login_progress_message_sincro_error));
            }
            @Override
            public void onErrorUnauthorized() {
                progress.dismiss();
                Util.toastIconError((AppCompatActivity) ctx,ctx.getResources().getString(R.string.login_erroruser_message));
                Util.logoutAndRedirect(ctx);
            }
        });
    }
}
