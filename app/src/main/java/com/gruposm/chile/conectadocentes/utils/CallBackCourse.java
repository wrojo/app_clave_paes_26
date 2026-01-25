package com.gruposm.chile.conectadocentes.utils;

import com.gruposm.chile.conectadocentes.object.Course;

import java.util.List;

public interface CallBackCourse {

    void onSucess(List<Course> courses);
    void onErrorServer();
    void onErrorUnauthorized();
}
