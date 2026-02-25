package cl.gruposm.conectaevaluaciones;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.PagerAdapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cl.gruposm.conectaevaluaciones.utils.SessionHelp;
import cl.gruposm.conectaevaluaciones.utils.Tools;

public class HelpActivity extends AppCompatActivity {

    private static final int MAX_STEP = 9;
    private SessionHelp sessionHelp;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private Button btnNext;
    private String about_title_array[] = {
            "Bienvenido a Clave PAES",
            "Crea tu curso en la plataforma",
            "Ensayos",
            "Libera tus ensayos",
            "Ensayos presenciales",
            "Hoja de respuestas",
            "Captura las hojas de respuestas",
            "Escaneo múltiple",
            "Comencemos"
    };
    private String about_description_array[] = {
            "Hemos preparado esta herramienta para facilitar la captura de las hojas de respuestas de tus ensayos Clave PAES",
            "Ingresa a la plataforma y crea tus clases. No olvides añadir a tus estudiantes. Luego, ingresa al curso para programar el ensayo.",
            "Clave PAES cuenta con ensayos de libre disposición del estudiante y otros que deben ser liberados por el docente.",
            "Los ensayos medidos por el docente pueden ser programados en una fecha determinada. Los estudiantes serán notificados con la fecha.",
            "Existen dos modalidades de rendición, en línea o presenciales.",
            "Para capturar las hojas respuestas de los estudiantes hemos creado una herramienta de IA que permite reconocer las respuestas del estudiante y enviarlas a la plataforma.",
            "Al ingresar al ensayo encontrarás un icono con forma de QR el que puedes utilizar para escanear la hoja de respuestas, la que será procesada de manera inmediata.",
            "Puedes escanear de manera secuencial las hojas de respuestas de todos los estudiantes.",
            "Puedes encontrar esta ayuda en el menú de la izquierda o consulta nuestros videos tutoriales."
    };
    private int about_images_array[] = {
            R.drawable.logo_paes,
            R.drawable.step01,
            R.drawable.step02,
            R.drawable.step03,
            R.drawable.step04,
            R.drawable.step05,
            R.drawable.step06,
            R.drawable.step07,
            R.drawable.step08
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initComponent();
        Tools.setSystemBarColor(this, R.color.paes_color_5);
        Tools.setSystemBarLight(this);
    }
    private void initComponent() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnNext = (Button) findViewById(R.id.btn_next);
        sessionHelp =  new SessionHelp(this);
        // adding bottom dots
        bottomProgressDots(0);

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < MAX_STEP) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    sessionHelp.createSession();
                    finish();
                }
            }
        });

        ((ImageButton)findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionHelp.createSession();
                finish();
            }
        });

    }

    private void bottomProgressDots(int current_index) {
        LinearLayout dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STEP];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            int width_height = 15;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
            params.setMargins(10, 10, 10, 10);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.shape_circle);
            dots[i].setColorFilter(getResources().getColor(R.color.grey_20), PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            dots[current_index].setImageResource(R.drawable.shape_circle);
            dots[current_index].setColorFilter(getResources().getColor(R.color.paes_color_2), PorterDuff.Mode.SRC_IN);
        }
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            bottomProgressDots(position);

            if (position == about_title_array.length - 1) {
                btnNext.setText(getString(R.string.GOT_IT));
                btnNext.setBackgroundColor(getResources().getColor(R.color.paes_color_2));
                btnNext.setTextColor(Color.WHITE);

            } else {
                btnNext.setText(getString(R.string.NEXT));
                btnNext.setBackgroundColor(getResources().getColor(R.color.paes_color_4));
                btnNext.setTextColor(getResources().getColor(R.color.white));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };
    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.item_stepper_wizard, container, false);
            ((TextView) view.findViewById(R.id.title)).setText(about_title_array[position]);
            ((TextView) view.findViewById(R.id.description)).setText(about_description_array[position]);
            ((ImageView) view.findViewById(R.id.image)).setImageResource(about_images_array[position]);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return about_title_array.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}