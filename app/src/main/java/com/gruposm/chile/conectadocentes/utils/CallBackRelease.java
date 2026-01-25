package com.gruposm.chile.conectadocentes.utils;

public interface CallBackRelease {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
}
