package cl.gruposm.conectaevaluaciones.utils;

import cl.gruposm.conectaevaluaciones.object.Course;

import java.util.List;

public interface CallBackCourse {

    void onSucess(List<Course> courses);
    void onErrorServer();
    void onErrorUnauthorized();
}
