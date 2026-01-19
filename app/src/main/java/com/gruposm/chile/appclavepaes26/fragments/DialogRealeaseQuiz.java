package com.gruposm.chile.appclavepaes26.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.gruposm.chile.appclavepaes26.R;
import com.gruposm.chile.appclavepaes26.database.ManageSql;
import com.gruposm.chile.appclavepaes26.object.Course;
import com.gruposm.chile.appclavepaes26.object.Quiz;
import com.gruposm.chile.appclavepaes26.services.ReleaseQuiz;
import com.gruposm.chile.appclavepaes26.utils.CallBackDialog;
import com.gruposm.chile.appclavepaes26.utils.CallBackRelease;
import com.gruposm.chile.appclavepaes26.utils.Tools;
import com.gruposm.chile.appclavepaes26.utils.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DialogRealeaseQuiz {
    private Context context;
    private Course course;
    private Quiz quiz;
    private String dateInit;
    private String timeInit;
    private String dateEnd;
    private String timeEnd;
    private CallBackDialog callBack;
    public DialogRealeaseQuiz(Context context, Quiz quiz, Course course, CallBackDialog callBack) {
        this.context = context;
        this.quiz = quiz;
        this.course = course;
        this.callBack = callBack;
        showCustomDialog();
    }
    private void showCustomDialog() {
        final Dialog dialog = new Dialog(this.context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_event);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final TextView text_course = (TextView) dialog.findViewById(R.id.text_course);
        text_course.setText(course.getNombre());

        final TextView text_quiz = (TextView) dialog.findViewById(R.id.text_quiz);
        text_quiz.setText(quiz.getNombre());

        final TextView text_level = (TextView) dialog.findViewById(R.id.text_level);
        text_level.setText(course.getNivel());
        //text_level.setText("");
        final EditText spn_from_date = (EditText) dialog.findViewById(R.id.spn_from_date);
        final EditText spn_from_time = (EditText) dialog.findViewById(R.id.spn_from_time);
        final EditText spn_to_date = (EditText) dialog.findViewById(R.id.spn_to_date);
        final EditText spn_to_time = (EditText) dialog.findViewById(R.id.spn_to_time);
        final AppCompatCheckBox cb_allday = (AppCompatCheckBox) dialog.findViewById(R.id.cb_allday);

        spn_from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight(v);
            }
        });
        spn_from_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTimePickerLight(v);
            }
        });

        spn_to_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight(v);
            }
        });
        spn_to_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogTimePickerLight(v);
            }
        });

        ((Button) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ((Button) dialog.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(quiz == null)
                {
                    Util.toastIconError((AppCompatActivity) context,context.getString(R.string.text_error_not_quiz_release_quiz));
                    return;
                }
                String valueDateInit  = spn_from_date.getText().toString().trim();
                String valueTimeInit = spn_from_time.getText().toString().trim();
                if (valueDateInit.isEmpty() ||  valueTimeInit.isEmpty())
                {
                    Util.toastIconError((AppCompatActivity) context,context.getString(R.string.text_error_validate_release_quiz));
                    spn_from_date.setError(context.getResources().getString(R.string.login_error_rut));
                    spn_from_time.setError(context.getResources().getString(R.string.login_error_clave));
                    return;
                }

                Map<String, String> data =  new HashMap<>();
                data.put("course_id",course.getId());
                data.put("quiz_id", quiz.getId());
                String notificate = "0";
                if(cb_allday.isChecked())
                {
                    notificate = "1";
                }
                String dateInitFinal = "";
                String dateEndFinal = "";

                if(dateInit != null && timeInit != null)
                {
                    dateInitFinal = dateInit + " "+ timeInit;
                }
                if(dateEnd != null && timeEnd != null)
                {
                    dateEndFinal = dateEnd + " "+ timeEnd;
                }
                data.put("fecha_inicio", dateInitFinal);
                data.put("fecha_termino", dateEndFinal);
                data.put("notificar", notificate);
                ReleaseQuiz releaseQuiz = new ReleaseQuiz(context, data, new CallBackRelease() {
                    @Override
                    public void onSucess(boolean success) {
                        dialog.dismiss();
                        if(success)
                        {
                            ManageSql manageSql =  new ManageSql(context);
                            long updateQuiz = manageSql.releaseQuiz(quiz,course);
                            if(updateQuiz > 0)
                            {
                                Util.toastIconSuccess((AppCompatActivity) context,context.getResources().getString(R.string.text_success_release_quiz));
                                callBack.onSuccess(true);
                            }
                            else
                            {
                                Util.toastIconError((AppCompatActivity) context,context.getString(R.string.text_error_release_quiz));
                            }
                            callBack.onSuccess(true);
                        }
                        else
                        {
                            Util.toastIconError((AppCompatActivity) context,context.getString(R.string.text_error_release_quiz));
                            callBack.onSuccess(false);
                        }
                    }
                    @Override
                    public void onErrorServer() {
                        Util.toastIconError((AppCompatActivity) context,context.getString(R.string.login_errorrespuesta_message));
                    }

                    @Override
                    public void onErrorUnauthorized() {
                        Util.toastIconError((AppCompatActivity) context,context.getString(R.string.login_erroruser_message));
                    }
                });

            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private void dialogDatePickerLight(final View v) {
        Calendar cur_calender = Calendar.getInstance();
        String sDate = String.valueOf(((EditText) v).getText());
        long time = Tools.getDateTime(sDate,"MMM dd, yyyy");
        if(time > -1)
        {
            cur_calender.setTime(new Date(time));
        }
        DatePickerDialog datePicker = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long date = calendar.getTimeInMillis();
                        ((EditText) v).setText(Tools.getFormattedDateShort(date));
                        String editName = context.getResources().getResourceEntryName(((EditText) v).getId());
                        if(editName.equals("spn_from_date"))
                        {
                            dateInit = Tools.getFormattedDateMysql(date);
                        }
                        if(editName.equals("spn_to_date"))
                        {
                            dateEnd = Tools.getFormattedDateMysql(date);
                        }
                    }

                },
                cur_calender.get(Calendar.YEAR),
                cur_calender.get(Calendar.MONTH),
                cur_calender.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((EditText) v).setText("");
            }
        });




        //set dark light
        datePicker.setThemeDark(false);
        datePicker.setAccentColor(context.getResources().getColor(R.color.paes_color_5));
        //datePicker.setMinDate(cur_calender);
        datePicker.show(((AppCompatActivity)context).getSupportFragmentManager(), "Expiration Date");
    }
    private void dialogTimePickerLight(final View v) {
        Calendar cur_calender = Calendar.getInstance();
        String sDate = String.valueOf(((EditText) v).getText());
        long time = Tools.getDateTime(sDate,"h:mm a");
        if(time > -1)
        {
            cur_calender.setTime(new Date(time));
        }
        TimePickerDialog datePicker = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.AM_PM, calendar.get(Calendar.AM_PM));
                long time_millis = calendar.getTimeInMillis();
                ((EditText) v).setText(Tools.getFormattedTimeEvent(time_millis));
                String editName = context.getResources().getResourceEntryName(((EditText) v).getId());
                if(editName.equals("spn_from_time"))
                {
                    timeInit = Tools.getFormattedTimeMysql(time_millis);
                }
                if(editName.equals("spn_to_time"))
                {
                    timeEnd = Tools.getFormattedTimeMysql(time_millis);
                }
            }
        }, cur_calender.get(Calendar.HOUR_OF_DAY), cur_calender.get(Calendar.MINUTE), true);
        datePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((EditText) v).setText("");
            }
        });
        //set dark light
        datePicker.setThemeDark(false);
        datePicker.setAccentColor(context.getResources().getColor(R.color.paes_color_5));
        datePicker.show(((AppCompatActivity)context).getSupportFragmentManager(), "Timepickerdialog");
    }
}
