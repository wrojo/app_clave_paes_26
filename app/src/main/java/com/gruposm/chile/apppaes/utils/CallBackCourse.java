package com.gruposm.chile.apppaes.utils;

import com.gruposm.chile.apppaes.object.Course;

import java.util.List;

public interface CallBackCourse {

    void onSucess(List<Course> courses);
    void onErrorServer();
    void onErrorUnauthorized();
}
