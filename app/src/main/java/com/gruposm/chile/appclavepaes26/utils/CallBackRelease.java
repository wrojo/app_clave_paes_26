package com.gruposm.chile.appclavepaes26.utils;

public interface CallBackRelease {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
}
