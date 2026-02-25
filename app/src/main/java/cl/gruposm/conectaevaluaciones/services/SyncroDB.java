package cl.gruposm.conectaevaluaciones.services;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import cl.gruposm.conectaevaluaciones.R;
import cl.gruposm.conectaevaluaciones.database.ManageSql;
import cl.gruposm.conectaevaluaciones.object.Course;
import cl.gruposm.conectaevaluaciones.utils.CallBackCourse;
import cl.gruposm.conectaevaluaciones.utils.CallBackSyncro;
import cl.gruposm.conectaevaluaciones.utils.Util;

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
