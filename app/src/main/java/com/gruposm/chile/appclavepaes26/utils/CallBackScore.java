package com.gruposm.chile.appclavepaes26.utils;

public interface CallBackScore {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorNoData();
}
