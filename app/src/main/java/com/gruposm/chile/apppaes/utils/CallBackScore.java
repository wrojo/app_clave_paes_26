package com.gruposm.chile.apppaes.utils;

public interface CallBackScore {

    void onSucess(boolean success);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorNoData();
}
