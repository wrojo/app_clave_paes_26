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

    private static final int MAX_STEP = 10;
    private SessionHelp sessionHelp;
    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private Button btnNext;
    private String about_title_array[] = {
            "Bienvenido a Conecta Evaluaciones",
            "Crea tu curso en la plataforma",
            "Evaluaciones disponibles",
            "Programa tu evaluación",
            "Modalidades de rendición",
            "Hoja de respuestas",
            "Escanea las hojas",
            "Enviar a Conecta",
            "Informes",
            "Comencemos"
    };
    private String about_description_array[] = {
            "Hemos preparado esta herramienta para facilitar la captura digital de las hojas de respuestas de las evaluaciones realizadas en SM Conecta v4.",
            "Ingresa a SM Conecta v4, crea tu curso y agrega a tus estudiantes. Desde el curso podrás programar evaluaciones para su aplicación.",
            "Las evaluaciones pueden estar disponibles para rendición en línea o en modalidad presencial según la planificación del docente.",
            "Las evaluaciones presenciales pueden ser programadas con fecha y hora específicas. Los estudiantes serán notificados desde la plataforma.",
            "Existen dos modalidades: evaluación en línea o aplicación presencial con hoja de respuestas impresa.",
            "Para las evaluaciones presenciales se utiliza una hoja de respuestas estandarizada que será procesada automáticamente por la aplicación.",
            "Dentro de cada evaluación encontrarás el ícono de escaneo. Utiliza la cámara del dispositivo para capturar la hoja de respuestas.",
            "Una vez completado el escaneo, las respuestas podrás enviarlas a SM Conecta v4 para su corrección y análisis.",
            "Podrás revisar los informes del curso y de tus estudiantes en la plataforma SM Conecta.",
            "Puedes revisar esta guía cuando lo necesites desde el menú lateral o consultar nuestros tutoriales."
    };
    private int about_images_array[] = {
            R.drawable.help_01,
            R.drawable.help_02,
            R.drawable.help_03,
            R.drawable.help_04,
            R.drawable.help_05,
            R.drawable.help_06,
            R.drawable.help_07,
            R.drawable.help_08,
            R.drawable.help_09,
            R.drawable.help_10
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
