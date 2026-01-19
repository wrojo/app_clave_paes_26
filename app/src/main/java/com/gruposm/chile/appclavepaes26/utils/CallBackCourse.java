package com.gruposm.chile.appclavepaes26.utils;

import com.gruposm.chile.appclavepaes26.object.Course;

import java.util.List;

public interface CallBackCourse {

    void onSucess(List<Course> courses);
    void onErrorServer();
    void onErrorUnauthorized();
}
