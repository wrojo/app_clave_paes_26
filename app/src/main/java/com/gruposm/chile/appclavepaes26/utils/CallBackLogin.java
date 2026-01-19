package com.gruposm.chile.appclavepaes26.utils;

import com.gruposm.chile.appclavepaes26.object.User;

public interface CallBackLogin {

    void onSucess(User user);
    void onErrorServer();
    void onErrorUnauthorized();
    void onErrorToken();
}
